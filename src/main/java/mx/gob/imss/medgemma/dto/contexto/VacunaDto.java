package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class VacunaDto {
    private String vacuna;
    private Integer numDosis;
    private String fechaAplicacion;
    private String lote;
}
