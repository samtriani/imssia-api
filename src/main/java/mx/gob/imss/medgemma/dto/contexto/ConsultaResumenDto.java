package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data @Builder
public class ConsultaResumenDto {
    private String fechaConsulta;
    private String tipoConsulta;
    private String signosVitales;
    private List<String> diagnosticos;
    private List<String> medicamentos;
    private String motivoConsulta;
    private String analisis;
    private String plan;
}
