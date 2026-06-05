package mx.gob.imss.medgemma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.LoginRequest;
import mx.gob.imss.medgemma.dto.response.LoginResponse;
import mx.gob.imss.medgemma.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login de médicos IMSS")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login del médico — retorna sesión con especialidad para el AI")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Médico no encontrado", "matricula", request.getMatricula()));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales incorrectas"));

        } catch (Exception e) {
            log.error("Error inesperado en login: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @GetMapping("/especialidad/{matricula}")
    @Operation(summary = "Obtener especialidad de un médico por matrícula (útil para debug)")
    public ResponseEntity<?> getEspecialidad(@PathVariable String matricula) {
        try {
            LoginRequest req = new LoginRequest();
            req.setMatricula(matricula);
            // Solo para consulta — no valida password, solo trae la especialidad
            // En producción proteger este endpoint con un token
            return ResponseEntity.ok(Map.of(
                "matricula", matricula,
                "nota", "Usar POST /login para autenticación completa"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
