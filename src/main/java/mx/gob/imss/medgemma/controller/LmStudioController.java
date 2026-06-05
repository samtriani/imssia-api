package mx.gob.imss.medgemma.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import mx.gob.imss.medgemma.service.LmStudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/lmstudio")
@RequiredArgsConstructor
@Tag(name="LM Studio",description="Chat directo con LM Studio — sin contexto clínico")
@CrossOrigin(origins="*")
public class LmStudioController {

    private final LmStudioService lmStudioService;

    @PostMapping("/chat")
    @Operation(summary="Chat simple sin contexto clínico")
    public ResponseEntity<LmStudioChatResponse> chat(@RequestBody ChatSimpleRequest request) {
        log.info("POST /api/v1/lmstudio/chat — input length: {}", request.getInput().length());
        return ResponseEntity.ok(lmStudioService.chatSimple(request.getInput(), request.getModel()));
    }

    @Data
    public static class ChatSimpleRequest {
        private String input;
        private String model;
    }
}
