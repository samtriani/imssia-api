package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class AntecedentePatologicoDto {
    private String cveCie10;
    private String padecimiento;
    private String fechaInicio;
    private String tratamientoActual;
    private Boolean indCronico;
    private Boolean indControlado;
    private String observaciones;
}
