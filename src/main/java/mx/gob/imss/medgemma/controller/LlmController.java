package mx.gob.imss.medgemma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.client.LmStudioClient;
import mx.gob.imss.medgemma.config.LmStudioConfig;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
@Tag(name = "LLM", description = "Proxy directo al servidor LLM (vLLM / llama.cpp / OpenAI-compatible)")
@CrossOrigin(origins = "*")
public class LlmController {

    private final LmStudioClient lmStudioClient;
    private final LmStudioConfig config;
    private final WebClient      lmStudioWebClient;

    /**
     * Proxy transparente: acepta el request OpenAI-compatible completo
     * (model, messages, max_tokens, temperature, top_p, repetition_penalty, stop)
     * y lo reenvía al servidor LLM activo.
     */
    @PostMapping("/chat")
    @Operation(summary = "Chat directo al LLM — formato OpenAI-compatible completo")
    public ResponseEntity<LmStudioChatResponse> chat(@RequestBody LmStudioChatRequest request) {
        int msgs = request.getMessages() != null ? request.getMessages().size() : 0;
        log.info("POST /api/v1/llm/chat — modelo: {}, mensajes: {}", request.getModel(), msgs);
        return ResponseEntity.ok(lmStudioClient.chat(request));
    }

    @GetMapping("/health")
    @Operation(summary = "Verifica conectividad con el servidor LLM (GET /v1/models)")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("GET /api/v1/llm/health — url: {}", config.getUrl());
        try {
            lmStudioWebClient.get()
                    .uri("/v1/models")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("LLM health UP — {}", config.getUrl());

            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("status",    "UP");
            ok.put("llm_url",   config.getUrl());
            ok.put("model",     config.getDefaultModel());
            ok.put("available", true);
            return ResponseEntity.ok(ok);

        } catch (Exception ex) {
            log.warn("LLM health DOWN — {} | {}", config.getUrl(), ex.getMessage());

            Map<String, Object> down = new LinkedHashMap<>();
            down.put("status",    "DOWN");
            down.put("llm_url",   config.getUrl());
            down.put("model",     config.getDefaultModel());
            down.put("available", false);
            down.put("error",     ex.getMessage() != null ? ex.getMessage() : "Connection failed");
            return ResponseEntity.status(503).body(down);
        }
    }
}
