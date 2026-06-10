package mx.gob.imss.medgemma.dto.request;

import lombok.Data;

@Data
public class OrientadorChatRequest {

    /** Pregunta del médico sobre cómo usar el sistema ECSUS */
    private String pregunta;

    /** Matrícula del médico — opcional, solo para el log de auditoría */
    private String numMatricula;
}
