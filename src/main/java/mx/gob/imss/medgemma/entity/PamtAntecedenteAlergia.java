package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamt_antecedente_alergia",schema="imss_ai")
public class PamtAntecedenteAlergia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_alergia") private Integer idAlergia;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false) private PamtExpedienteClinico expediente;
    @Column(name="des_alergeno",nullable=false) private String desAlergeno;
    @Column(name="des_tipo") private String desTipo;
    @Column(name="des_reaccion",columnDefinition="TEXT") private String desReaccion;
    @Column(name="des_severidad") private String desSeveridad;
    @Column(name="stp_alta") private Instant stpAlta;
}
