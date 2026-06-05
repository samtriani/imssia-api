package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
@Data @Entity @Table(name="pamc_unidad_medica",schema="imss_ai")
public class PamcUnidadMedica {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_unidad_medica") private Integer idUnidadMedica;
    @Column(name="cve_unidad",nullable=false,unique=true) private String cveUnidad;
    @Column(name="des_unidad",nullable=false) private String desUnidad;
    @Column(name="des_nivel") private String desNivel;
    @Column(name="des_municipio") private String desMunicipio;
    @Column(name="des_direccion") private String desDireccion;
    @Column(name="ind_activo") private Boolean indActivo=true;
}
