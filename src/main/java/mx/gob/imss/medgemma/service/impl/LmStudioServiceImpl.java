package mx.gob.imss.medgemma.service.impl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.client.LmStudioClient;
import mx.gob.imss.medgemma.config.LmStudioConfig;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import mx.gob.imss.medgemma.service.LmStudioService;
import org.springframework.stereotype.Service;
@Slf4j @Service @RequiredArgsConstructor
public class LmStudioServiceImpl implements LmStudioService {
    private final LmStudioClient client;
    private final LmStudioConfig config;

    @Override
    public LmStudioChatResponse chatSimple(String input) {
        return chatSimple(input, null);
    }

    @Override
    public LmStudioChatResponse chatSimple(String input, String model) {
        LmStudioChatRequest req = LmStudioChatRequest.builder()
            .model(model != null ? model : config.getDefaultModel())
            .messages(java.util.List.of(LmStudioChatRequest.userMsg(input)))
            .maxTokens(config.getMaxTokens())
            .temperature(config.getTemperature())
            .build();
        log.info("chatSimple — modelo: {}, chars: {}", req.getModel(), input.length());
        LmStudioChatResponse res = client.chat(req);
        log.info("Respuesta recibida — tokens: {}, tok/s: {}",
            res.getStats() != null ? res.getStats().getTotalOutputTokens() : "N/A",
            res.getStats() != null ? res.getStats().getTokensPerSecond() : "N/A");
        return res;
    }
}
