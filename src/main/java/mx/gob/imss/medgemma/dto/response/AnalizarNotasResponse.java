package mx.gob.imss.medgemma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AnalizarNotasResponse {
    private List<ProcedimientoDetectadoDto> procedimientosDetectados;
    private long diasHospitalizacion;
    private BigDecimal totalBase;
    private BigDecimal total1erNivel;
    private BigDecimal total2doNivel;
    private BigDecimal total3erNivel;
    private int totalProcedimientos;
}
