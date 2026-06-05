package mx.gob.imss.medgemma.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.contexto.PacienteContextoDto;
import mx.gob.imss.medgemma.dto.request.ImssAiChatRequest;
import mx.gob.imss.medgemma.dto.response.ImssAiChatResponse;
import mx.gob.imss.medgemma.service.ImssAiContextService;
import mx.gob.imss.medgemma.service.PacienteContextoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/imss-ai")
@RequiredArgsConstructor
@Tag(name="IMSS AI",description="Chat médico con contexto clínico del paciente")
@CrossOrigin(origins="*")
public class ImssAiController {

    private final ImssAiContextService imssAiContextService;
    private final PacienteContextoService contextoService;

    @PostMapping("/chat")
    @Operation(summary="Chat con contexto clínico — principal endpoint del IMSS AI")
    public ResponseEntity<ImssAiChatResponse> chat(@RequestBody ImssAiChatRequest request) {
        log.info("POST /api/v1/imss-ai/chat — CURP: {}", request.getCurp());
        return ResponseEntity.ok(imssAiContextService.chatConContexto(request));
    }

    @GetMapping("/contexto/{curp}")
    @Operation(summary="Obtener contexto clínico ensamblado (debug/preview)")
    public ResponseEntity<PacienteContextoDto> getContexto(@PathVariable String curp) {
        log.info("GET /api/v1/imss-ai/contexto/{}", curp);
        return ResponseEntity.ok(contextoService.ensamblarContexto(curp));
    }

    @GetMapping("/contexto/{curp}/prompt-preview")
    @Operation(summary="Ver el prompt completo que se manda a MedGemma (debug)")
    public ResponseEntity<String> getPromptPreview(@PathVariable String curp,
                                                    @RequestParam String pregunta) {
        PacienteContextoDto ctx = contextoService.ensamblarContexto(curp);
        String prompt = ctx.toPromptText() + "\n\nPREGUNTA DEL MÉDICO:\n" + pregunta;
        return ResponseEntity.ok(prompt);
    }
}
