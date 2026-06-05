package mx.gob.imss.medgemma.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "pamc_costo_procedimiento", schema = "imss_ai")
public class PamcCostoProcedimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_procedimiento")
    private Integer idProcedimiento;

    @Column(name = "cve_procedimiento", nullable = false, unique = true, length = 50)
    private String cveProcedimiento;

    @Column(name = "des_procedimiento", nullable = false, length = 150)
    private String desProcedimiento;

    @Column(name = "num_costo_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal numCostoBase;

    @Column(name = "num_costo_1er_nivel", precision = 12, scale = 2)
    private BigDecimal numCosto1erNivel;

    @Column(name = "num_costo_2do_nivel", precision = 12, scale = 2)
    private BigDecimal numCosto2doNivel;

    @Column(name = "num_costo_3er_nivel", precision = 12, scale = 2)
    private BigDecimal numCosto3erNivel;

    @Column(name = "ind_activo", nullable = false)
    private Boolean indActivo = true;

    @Column(name = "stp_creacion", nullable = false)
    private Instant stpCreacion;
}
