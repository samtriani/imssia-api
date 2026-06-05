package mx.gob.imss.medgemma.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultaBriefDto {
    private String idConsulta;
    private String fecConsulta;
    private String desTipoConsulta;
    private String desMotivoConsulta;
    private String nombreMedico;
    private String textoNota;
}
