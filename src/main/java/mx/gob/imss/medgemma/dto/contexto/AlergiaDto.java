package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class AlergiaDto {
    private String alergeno;
    private String tipo;
    private String reaccion;
    private String severidad;
}
