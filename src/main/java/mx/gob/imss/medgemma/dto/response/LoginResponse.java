package mx.gob.imss.medgemma.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response del endpoint POST /api/v1/auth/login
 * Contiene los datos de sesión del médico, incluyendo
 * su especialidad para que el frontend la use en cada request al AI.
 */
@Data
@Builder
public class LoginResponse {

    private String  matricula;
    private String  nombre;           // "SAMUEL ANTONIO PARTIDA CONTRERAS"
    private String  nombreCorto;      // "Dr. Partida"
    private String  email;
    private String  turno;            // MATUTINO / VESPERTINO / NOCTURNO
    private String  nivel;            // MEDICO_GENERAL / ESPECIALISTA / RESIDENTE

    /**
     * Clave de especialidad — usada directamente en SystemPromptBuilder.
     * Valores: MEDICINA_GENERAL, ENDOCRINOLOGIA, CARDIOLOGIA, PEDIATRIA, GINECOLOGIA
     */
    private String  cveEspecialidad;

    /** Nombre completo de la especialidad para mostrar en UI */
    private String  desEspecialidad;

    /** Unidad médica de adscripción */
    private String  unidadMedica;
}
