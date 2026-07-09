package mx.gob.imss.medgemma.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.builder.GuiaTopicDetector;
import mx.gob.imss.medgemma.builder.SystemPromptBuilder;
import mx.gob.imss.medgemma.client.LmStudioClient;
import mx.gob.imss.medgemma.config.LmStudioConfig;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.request.OrientadorChatRequest;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import mx.gob.imss.medgemma.dto.response.OrientadorChatResponse;
import mx.gob.imss.medgemma.entity.PamtRagConsultaLog;
import mx.gob.imss.medgemma.repository.PamtRagConsultaLogRepository;
import mx.gob.imss.medgemma.service.GuiaSistemaService;
import mx.gob.imss.medgemma.service.OrientadorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrientadorServiceImpl implements OrientadorService {

    private final GuiaSistemaService           guiaSistemaService;
    private final LmStudioClient               lmStudioClient;
    private final LmStudioConfig               config;
    private final PamtRagConsultaLogRepository logRepo;

    /** Si es false, el Orientador NO escribe en imss_ai.pamt_rag_consulta_log. */
    @org.springframework.beans.factory.annotation.Value("${orientador.rag-log-enabled:true}")
    private boolean ragLogEnabled;

    @Override
    public OrientadorChatResponse chat(OrientadorChatRequest request) {
        log.info("Orientador chat — matrícula: {} | pregunta: {}", request.getNumMatricula(), request.getPregunta());

        Preparado p = preparar(request, false);
        log.info("Orientador contexto — tema: {} | modo: {} | detalle: {}",
                p.tema(), p.contextoSelectivo() ? "guia-unica" : "todas-las-guias", p.detallado());

        LmStudioChatResponse lmResponse = lmStudioClient.chat(p.lmRequest());
        log.info("Orientador respuesta — tema: {} | imágenes: {} | videos: {}",
                p.tema(), p.imagenes().size(), p.videos().size());

        guardarLog(request, lmResponse, p.tema());

        return OrientadorChatResponse.builder()
                .respuesta(lmResponse.getContent())
                .responseId(lmResponse.getResponseId())
                .modelInstanceId(lmResponse.getModelInstanceId())
                .totalOutputTokens(lmResponse.getStats() != null ? lmResponse.getStats().getTotalOutputTokens() : null)
                .tokensPerSecond(lmResponse.getStats() != null ? lmResponse.getStats().getTokensPerSecond() : null)
                .temaDetectado(p.tema())
                .imagenesRelacionadas(p.imagenes())
                .videosRelacionados(p.videos())
                .build();
    }

    @Override
    public Flux<ServerSentEvent<Object>> chatStream(OrientadorChatRequest request) {
        log.info("Orientador chat STREAM — matrícula: {} | pregunta: {}", request.getNumMatricula(), request.getPregunta());

        Preparado p = preparar(request, true);
        log.info("Orientador contexto (stream) — tema: {} | modo: {} | detalle: {}",
                p.tema(), p.contextoSelectivo() ? "guia-unica" : "todas-las-guias", p.detallado());

        // Evento inicial con los metadatos (ya conocidos antes de generar): tema, imágenes y videos.
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("temaDetectado", p.tema());
        metaData.put("imagenesRelacionadas", p.imagenes());
        metaData.put("videosRelacionados", p.videos());
        ServerSentEvent<Object> meta = ServerSentEvent.<Object>builder().event("meta").data(metaData).build();

        // Fragmentos de texto conforme el LLM los genera.
        Flux<ServerSentEvent<Object>> deltas = lmStudioClient.chatStream(p.lmRequest())
                .map(texto -> ServerSentEvent.<Object>builder().event("delta").data(Map.of("text", texto)).build());

        ServerSentEvent<Object> done = ServerSentEvent.<Object>builder().event("done").data(Map.of("done", true)).build();

        return Flux.concat(Flux.just(meta), deltas, Flux.just(done));
    }

    /** Prepara tema, contexto (RAG selectivo), nivel de detalle y el request al LLM (compartido por chat y chatStream). */
    private Preparado preparar(OrientadorChatRequest request, boolean stream) {
        // 1) Detectar el tema ANTES de llamar al LLM, para inyectar solo la guía relevante (RAG selectivo).
        String tema = GuiaTopicDetector.detectarTema(request.getPregunta());

        // 2) Contexto: solo la guía del tema detectado; si no se detecta tema, fallback a todas las guías.
        String contexto = tema != null ? guiaSistemaService.obtenerContextoPorTema(tema) : null;
        boolean contextoSelectivo = contexto != null;
        if (contexto == null) {
            contexto = guiaSistemaService.obtenerContextoGuias();
        }

        // 3) Nivel de detalle: si la pregunta pide una explicación detallada, ampliar el prompt y dar más margen de tokens.
        boolean detallado = solicitaDetalle(request.getPregunta());
        String systemPrompt = SystemPromptBuilder.buildOrientador() + "\n\n" + contexto
                + (detallado ? "\n\n" + SystemPromptBuilder.instruccionDetalle() : "");

        LmStudioChatRequest lmRequest = LmStudioChatRequest.builder()
                .model(config.getDefaultModel())
                .messages(List.of(
                        LmStudioChatRequest.systemMsg(systemPrompt),
                        LmStudioChatRequest.userMsg(request.getPregunta())
                ))
                .maxTokens(detallado ? Math.max(config.getMaxTokens(), 2048) : config.getMaxTokens())
                .temperature(config.getTemperature())
                .stream(stream ? Boolean.TRUE : null)
                .build();

        List<String> imagenes = tema != null ? guiaSistemaService.obtenerImagenesPorTema(tema) : List.of();
        List<String> videos   = guiaSistemaService.obtenerVideos(request.getPregunta(), tema);

        return new Preparado(tema, contextoSelectivo, detallado, lmRequest, imagenes, videos);
    }

    private record Preparado(String tema, boolean contextoSelectivo, boolean detallado,
                             LmStudioChatRequest lmRequest, List<String> imagenes, List<String> videos) {}

    /** Detecta si la pregunta solicita una explicación detallada/ampliada. */
    private static boolean solicitaDetalle(String pregunta) {
        if (pregunta == null) return false;
        String t = pregunta.toLowerCase();
        return t.contains("a detalle") || t.contains("detallad")
                || t.contains("a fondo") || t.contains("con detalle")
                || t.contains("más detalle") || t.contains("mas detalle")
                || t.contains("explica bien") || t.contains("explícame bien") || t.contains("explicame bien")
                || t.contains("explicación detallada") || t.contains("explicacion detallada")
                || t.contains("lo más completo") || t.contains("lo mas completo");
    }

    private void guardarLog(OrientadorChatRequest req, LmStudioChatResponse res, String tema) {
        if (!ragLogEnabled) {
            log.debug("RAG log del Orientador deshabilitado (orientador.rag-log-enabled=false) — no se escribe pamt_rag_consulta_log");
            return;
        }
        try {
            PamtRagConsultaLog entry = new PamtRagConsultaLog();
            entry.setDesQuery("[ORIENTADOR] " + req.getPregunta() + (tema != null ? " [tema:" + tema + "]" : ""));
            entry.setDesRespuesta(res.getContent());
            entry.setDesModelo(config.getDefaultModel());
            entry.setStpConsulta(Instant.now());
            if (res.getStats() != null) {
                entry.setNumTokensSalida(res.getStats().getTotalOutputTokens());
                if (res.getStats().getTokensPerSecond() != null)
                    entry.setNumTokensPorSeg(BigDecimal.valueOf(res.getStats().getTokensPerSecond()));
            }
            logRepo.save(entry);
        } catch (Exception e) {
            log.warn("No se pudo guardar log RAG del Orientador: {}", e.getMessage());
        }
    }
}
