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

    @Override
    public OrientadorChatResponse chat(OrientadorChatRequest request) {
        log.info("Orientador chat — matrícula: {} | pregunta: {}", request.getNumMatricula(), request.getPregunta());

        String promptFinal = SystemPromptBuilder.buildOrientador()
                + "\n\n" + guiaSistemaService.obtenerContextoGuias()
                + "\n\n══ PREGUNTA DEL USUARIO ══\n" + request.getPregunta();

        LmStudioChatRequest lmRequest = LmStudioChatRequest.builder()
                .model(config.getDefaultModel())
                .messages(java.util.List.of(LmStudioChatRequest.userMsg(promptFinal)))
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LmStudioChatResponse lmResponse = lmStudioClient.chat(lmRequest);

        String tema = GuiaTopicDetector.detectarTema(request.getPregunta());
        List<String> imagenes = tema != null ? guiaSistemaService.obtenerImagenesPorTema(tema) : List.of();

        log.info("Orientador respuesta — tema detectado: {} | imágenes: {}", tema, imagenes.size());

        guardarLog(request, lmResponse, tema);

        return OrientadorChatResponse.builder()
                .respuesta(lmResponse.getContent())
                .responseId(lmResponse.getResponseId())
                .modelInstanceId(lmResponse.getModelInstanceId())
                .totalOutputTokens(lmResponse.getStats() != null ? lmResponse.getStats().getTotalOutputTokens() : null)
                .tokensPerSecond(lmResponse.getStats() != null ? lmResponse.getStats().getTokensPerSecond() : null)
                .temaDetectado(tema)
                .imagenesRelacionadas(imagenes)
                .build();
    }

    private void guardarLog(OrientadorChatRequest req, LmStudioChatResponse res, String tema) {
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
