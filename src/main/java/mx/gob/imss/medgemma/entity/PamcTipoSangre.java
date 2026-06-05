package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
@Data @Entity @Table(name="pamc_tipo_sangre",schema="imss_ai")
public class PamcTipoSangre {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_tipo_sangre") private Integer idTipoSangre;
    @Column(name="des_tipo_sangre",nullable=false,unique=true) private String desTipoSangre;
}
