package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
@Data @Builder
public class AntecedenteNoPatologicoDto {
    private String tabaquismo;
    private Integer numCigarrosDia;
    private String alcoholismo;
    private String actividadFisica;
    private String alimentacion;
    private String ocupacion;
    private String escolaridad;
}
