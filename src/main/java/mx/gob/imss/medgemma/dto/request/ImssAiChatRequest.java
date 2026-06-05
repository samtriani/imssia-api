package mx.gob.imss.medgemma.dto.request;
import lombok.Data;

@Data
public class ImssAiChatRequest {

    private String curp;
    private String numMatricula;
    private String pregunta;
    private String especialidadMedico;
    private String modelo;

    /** Texto extraído de la nota médica (PDF/Word) subida por el médico */
    private String notaMedica;

    /** Fecha de ingreso hospitalario — formato yyyy-MM-dd */
    private String fechaIngreso;

    /** Fecha de egreso hospitalario — formato yyyy-MM-dd */
    private String fechaEgreso;
}