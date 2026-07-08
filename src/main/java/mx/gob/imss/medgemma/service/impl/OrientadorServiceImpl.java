package mx.gob.imss.medgemma.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

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

        // 1) Detectar el tema ANTES de llamar al LLM, para inyectar solo la guía relevante (RAG selectivo).
        String tema = GuiaTopicDetector.detectarTema(request.getPregunta());

        // 2) Contexto: solo la guía del tema detectado; si no se detecta tema, fallback a todas las guías.
        String contexto = tema != null ? guiaSistemaService.obtenerContextoPorTema(tema) : null;
        boolean contextoSelectivo = contexto != null;
        if (contexto == null) {
            contexto = guiaSistemaService.obtenerContextoGuias();
        }

        String systemPrompt = SystemPromptBuilder.buildOrientador() + "\n\n" + contexto;
        log.info("Orientador contexto — tema: {} | modo: {} | chars contexto: {}",
                tema, contextoSelectivo ? "guia-unica" : "todas-las-guias", contexto.length());

        LmStudioChatRequest lmRequest = LmStudioChatRequest.builder()
                .model(config.getDefaultModel())
                .messages(java.util.List.of(
                        LmStudioChatRequest.systemMsg(systemPrompt),
                        LmStudioChatRequest.userMsg(request.getPregunta())
                ))
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LmStudioChatResponse lmResponse = lmStudioClient.chat(lmRequest);

        List<String> imagenes = tema != null ? guiaSistemaService.obtenerImagenesPorTema(tema) : List.of();
        List<String> videos   = guiaSistemaService.obtenerVideos(request.getPregunta(), tema);

        log.info("Orientador respuesta — tema: {} | imágenes: {} | videos: {}", tema, imagenes.size(), videos.size());

        guardarLog(request, lmResponse, tema);

        return OrientadorChatResponse.builder()
                .respuesta(lmResponse.getContent())
                .responseId(lmResponse.getResponseId())
                .modelInstanceId(lmResponse.getModelInstanceId())
                .totalOutputTokens(lmResponse.getStats() != null ? lmResponse.getStats().getTotalOutputTokens() : null)
                .tokensPerSecond(lmResponse.getStats() != null ? lmResponse.getStats().getTokensPerSecond() : null)
                .temaDetectado(tema)
                .imagenesRelacionadas(imagenes)
                .videosRelacionados(videos)
                .build();
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
