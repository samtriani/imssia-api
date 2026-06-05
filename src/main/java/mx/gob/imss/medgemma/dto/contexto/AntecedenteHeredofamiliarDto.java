package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class AntecedenteHeredofamiliarDto {
    private String parentesco;
    private String cveCie10;
    private String padecimiento;
    private String observaciones;
}
