package mx.gob.imss.medgemma.client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
@Slf4j @Component @RequiredArgsConstructor
public class LmStudioClient {
    private final WebClient lmStudioWebClient;

    public LmStudioChatResponse chat(LmStudioChatRequest request) {
        log.info("POST /api/v1/chat — modelo: {}, chars: {}", request.getModel(),
            request.getInput() != null ? request.getInput().length() : 0);
        try {
            return lmStudioWebClient.post().uri("/api/v1/chat")
                .bodyValue(request).retrieve()
                .bodyToMono(LmStudioChatResponse.class).block();
        } catch (WebClientResponseException ex) {
            log.error("Error LM Studio: {} — {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
