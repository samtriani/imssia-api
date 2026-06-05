package mx.gob.imss.medgemma.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.LoginRequest;
import mx.gob.imss.medgemma.dto.response.LoginResponse;
import mx.gob.imss.medgemma.entity.PamtUsuarioMedico;
import mx.gob.imss.medgemma.repository.PamtUsuarioMedicoRepository;
import mx.gob.imss.medgemma.service.AuthService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PamtUsuarioMedicoRepository medicoRepo;
    private final JdbcTemplate                jdbcTemplate;

    /**
     * Mapa cve_especialidad (BD) → clave del SystemPromptBuilder.
     * IMPORTANTE: Map.of() solo admite 10 entradas — usar Map.ofEntries() para 20.
     */
    private static final Map<String, String> ESPECIALIDAD_A_SKILL = Map.ofEntries(
        Map.entry("MED_GEN",        "MED_GEN"),
        Map.entry("MED_FAM",        "MED_FAM"),
        Map.entry("MED_INT",        "MED_INT"),
        Map.entry("PEDIATRIA",      "PEDIATRIA"),
        Map.entry("GINECOOBST",     "GINECOOBST"),
        Map.entry("CIRUGIA_GEN",    "CIRUGIA_GEN"),
        Map.entry("TRAUMATOLOGIA",  "TRAUMATOLOGIA"),
        Map.entry("CARDIOLOGIA",    "CARDIOLOGIA"),
        Map.entry("NEUROLOGIA",     "NEUROLOGIA"),
        Map.entry("PSIQUIATRIA",    "PSIQUIATRIA"),       // ← antes apuntaba a MEDICINA_GENERAL
        Map.entry("DERMATOLOGIA",   "DERMATOLOGIA"),
        Map.entry("OFTALMOLOGIA",   "OFTALMOLOGIA"),
        Map.entry("OTORRINOL",      "OTORRINOL"),
        Map.entry("ENDOCRINOL",     "ENDOCRINOL"),
        Map.entry("NEUMOLOGIA",     "NEUMOLOGIA"),
        Map.entry("GASTROENT",      "GASTROENT"),
        Map.entry("ONCOLOGIA",      "ONCOLOGIA"),
        Map.entry("URGENCIAS",      "URGENCIAS"),
        Map.entry("ANESTESIOLOGIA", "ANESTESIOLOGIA"),
        Map.entry("RADIOLOGIA",     "RADIOLOGIA")
    );

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt — matrícula: {}", request.getMatricula());

        // 1. Buscar médico activo por matrícula
        PamtUsuarioMedico medico = medicoRepo
            .findByNumMatriculaAndIndActivoTrue(request.getMatricula())
            .orElseThrow(() -> new EntityNotFoundException(
                "Médico no encontrado: " + request.getMatricula()));

        // 2. Validar password con bcrypt via pgcrypto
        boolean passwordValido = validarPassword(request.getPassword(), medico.getDesPasswordHash());
        if (!passwordValido) {
            log.warn("Password incorrecto para matrícula: {}", request.getMatricula());
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // 3. Obtener cve_especialidad de la relación @ManyToOne
        String cveEspecialidad = medico.getEspecialidad() != null
            ? medico.getEspecialidad().getCveEspecialidad()
            : "MED_GEN";

        String desEspecialidad = medico.getEspecialidad() != null
            ? medico.getEspecialidad().getDesEspecialidad()
            : "Medicina General";

        // 4. Mapear cve → skill key (1:1 — la cve ya es la key del SystemPromptBuilder)
        String skillKey = ESPECIALIDAD_A_SKILL.getOrDefault(cveEspecialidad, "MED_GEN");

        log.info("Login OK — matrícula: {} | especialidad: {} | skill: {}",
            request.getMatricula(), cveEspecialidad, skillKey);

        // 5. Construir nombres
        String nombreCorto = "Dr. " + medico.getNomPrimerApellido();
        String nombreCompleto = medico.getNomNombre()
            + " " + medico.getNomPrimerApellido()
            + (medico.getNomSegundoApellido() != null ? " " + medico.getNomSegundoApellido() : "");

        return LoginResponse.builder()
            .matricula(medico.getNumMatricula())
            .nombre(nombreCompleto.trim())
            .nombreCorto(nombreCorto)
            .email(medico.getDesEmail())
            .turno(medico.getDesTurno())
            .nivel(medico.getDesNivel())
            .cveEspecialidad(skillKey)
            .desEspecialidad(desEspecialidad)
            .unidadMedica(medico.getUnidadMedica() != null
                ? medico.getUnidadMedica().getDesUnidad()
                : null)
            .build();
    }

    /**
     * Valida el password usando pgcrypto en PostgreSQL.
     * crypt(input, stored_hash) = stored_hash → TRUE si coinciden.
     */
    private boolean validarPassword(String passwordInput, String storedHash) {
        try {
            Boolean resultado = jdbcTemplate.queryForObject(
                "SELECT crypt(?, ?) = ?",
                Boolean.class,
                passwordInput,
                storedHash,
                storedHash
            );
            return Boolean.TRUE.equals(resultado);
        } catch (Exception e) {
            log.error("Error validando password con pgcrypto: {}", e.getMessage());
            return false;
        }
    }
}