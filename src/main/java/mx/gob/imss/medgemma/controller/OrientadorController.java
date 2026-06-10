package mx.gob.imss.medgemma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.OrientadorChatRequest;
import mx.gob.imss.medgemma.dto.response.OrientadorChatResponse;
import mx.gob.imss.medgemma.service.OrientadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
