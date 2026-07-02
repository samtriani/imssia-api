package mx.gob.imss.medgemma.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.config.LmStudioConfig;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j @Component @RequiredArgsConstructor
public class LmStudioClient {

    private final WebClient      lmStudioWebClient;
    private final LmStudioConfig config;

    public LmStudioChatResponse chat(LmStudioChatRequest request) {
        LmStudioChatRequest enriched = enrich(request);

        try {
            String requestJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(enriched);
            log.info("REQUEST JSON enviado a LLM:\n{}", requestJson);
        } catch (Exception ex) {
            log.warn("No se pudo serializar request para logging: {}", ex.getMessage());
        }
        int chars = (enriched.getMessages() != null)
                ? enriched.getMessages().stream().mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0).sum() : 0;
        log.info("POST /v1/chat/completions — modelo: {}, chars total: {}, msgs: {}, minTokens: {}, topP: {}, repPenalty: {}, stop: {}",
                enriched.getModel(), chars, enriched.getMessages() != null ? enriched.getMessages().size() : 0,
                enriched.getMinTokens(), enriched.getTopP(), enriched.getRepetitionPenalty(), enriched.getStop());

        try {
            String rawBody = lmStudioWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(enriched)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("RAW response LLM:\n{}", rawBody);

            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(rawBody, LmStudioChatResponse.class);

        } catch (WebClientResponseException ex) {
            log.error("Error LLM {} — url: {} — body: {}", ex.getStatusCode(),
                    config.getUrl(), ex.getResponseBodyAsString());
            throw ex;
        } catch (Exception ex) {
            log.error("Error deserializando response LLM — {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Aplica defaults del config solo cuando el caller no especificó el valor.
     * Prioridad: valor del request > valor del config.
     */
    private LmStudioChatRequest enrich(LmStudioChatRequest req) {
        return LmStudioChatRequest.builder()
                .model(req.getModel() != null ? req.getModel() : config.getDefaultModel())
                .messages(req.getMessages())
                .maxTokens(req.getMaxTokens() != null           ? req.getMaxTokens()           : config.getMaxTokens())
                .minTokens(req.getMinTokens() != null           ? req.getMinTokens()           : config.getMinTokens())
                .temperature(req.getTemperature() != null       ? req.getTemperature()          : config.getTemperature())
                .topP(req.getTopP() != null                     ? req.getTopP()                 : config.getTopP())
                .repetitionPenalty(req.getRepetitionPenalty() != null
                                                                ? req.getRepetitionPenalty()    : config.getRepetitionPenalty())
                .stop(req.getStop() != null && !req.getStop().isEmpty()
                                                                ? req.getStop()                 : config.getStop())
                .build();
    }
}
