package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamc_medicamento",schema="imss_ai")
public class PamcMedicamento {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_medicamento") private Integer idMedicamento;
    @Column(name="cve_cuadro_basico",unique=true) private String cveCuadroBasico;
    @Column(name="des_medicamento",nullable=false) private String desMedicamento;
    @Column(name="des_principio_activo") private String desPrincipioActivo;
    @Column(name="des_presentacion") private String desPresentacion;
    @Column(name="des_dosis_usual") private String desDosisUsual;
    @Column(name="ind_controlado") private Boolean indControlado=false;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
}
