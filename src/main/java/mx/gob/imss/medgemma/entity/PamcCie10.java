package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
@Data @Entity @Table(name="pamc_cie10",schema="imss_ai")
public class PamcCie10 {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_cie10") private Integer idCie10;
    @Column(name="cve_cie10",nullable=false,unique=true) private String cveCie10;
    @Column(name="des_diagnostico",nullable=false) private String desDiagnostico;
    @Column(name="des_categoria") private String desCategoria;
    @Column(name="ind_activo") private Boolean indActivo=true;
}
