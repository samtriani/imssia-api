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

        int chars = (enriched.getMessages() != null && !enriched.getMessages().isEmpty())
                ? enriched.getMessages().get(0).getContent().length() : 0;
        log.info("POST /v1/chat/completions — modelo: {}, chars: {}, topP: {}, repPenalty: {}",
                enriched.getModel(), chars, enriched.getTopP(), enriched.getRepetitionPenalty());

        try {
            return lmStudioWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(enriched)
                    .retrieve()
                    .bodyToMono(LmStudioChatResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error LLM {}: {} — {}", ex.getStatusCode(),
                    config.getUrl(), ex.getResponseBodyAsString());
            throw ex;
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
                .temperature(req.getTemperature() != null       ? req.getTemperature()          : config.getTemperature())
                .topP(req.getTopP() != null                     ? req.getTopP()                 : config.getTopP())
                .repetitionPenalty(req.getRepetitionPenalty() != null
                                                                ? req.getRepetitionPenalty()    : config.getRepetitionPenalty())
                .stop(req.getStop() != null && !req.getStop().isEmpty()
                                                                ? req.getStop()                 : config.getStop())
                .build();
    }
}
