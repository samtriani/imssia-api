package mx.gob.imss.medgemma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.OrientadorChatRequest;
import mx.gob.imss.medgemma.dto.response.OrientadorChatResponse;
import mx.gob.imss.medgemma.service.OrientadorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/orientador")
@RequiredArgsConstructor
@Tag(name="Orientador del Sistema", description="Asistente de navegación y uso del sistema clínico ECSUS")
@CrossOrigin(origins="*")
public class OrientadorController {

    private final OrientadorService orientadorService;

    @PostMapping("/chat")
    @Operation(summary="Chat del Orientador del Sistema ECSUS")
    public ResponseEntity<OrientadorChatResponse> chat(@RequestBody OrientadorChatRequest request) {
        log.info("POST /api/v1/orientador/chat — pregunta: {}", request.getPregunta());
        return ResponseEntity.ok(orientadorService.chat(request));
    }

    @PostMapping(value="/chat/stream", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary="Chat del Orientador en streaming (SSE): eventos meta, delta y done")
    public Flux<ServerSentEvent<Object>> chatStream(@RequestBody OrientadorChatRequest request) {
        log.info("POST /api/v1/orientador/chat/stream — pregunta: {}", request.getPregunta());
        return orientadorService.chatStream(request);
    }
}
