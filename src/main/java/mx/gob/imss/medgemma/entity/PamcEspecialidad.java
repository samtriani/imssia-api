package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamc_especialidad",schema="imss_ai")
public class PamcEspecialidad {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_especialidad") private Integer idEspecialidad;
    @Column(name="cve_especialidad",nullable=false,unique=true) private String cveEspecialidad;
    @Column(name="des_especialidad",nullable=false) private String desEspecialidad;
    @Column(name="des_abreviatura") private String desAbreviatura;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
}
