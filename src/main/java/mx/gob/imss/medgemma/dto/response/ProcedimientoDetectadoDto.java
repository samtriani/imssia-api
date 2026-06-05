package mx.gob.imss.medgemma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProcedimientoDetectadoDto {
    private String     cveProcedimiento;
    private String     desProcedimiento;
    private BigDecimal numCostoBase;
    private BigDecimal numCosto1erNivel;
    private BigDecimal numCosto2doNivel;
    private BigDecimal numCosto3erNivel;
}
