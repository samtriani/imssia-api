package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamt_antecedente_heredofamiliar",schema="imss_ai")
public class PamtAntecedenteHeredofamiliar {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_antecedente") private Integer idAntecedente;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false) private PamtExpedienteClinico expediente;
    @Column(name="des_parentesco",nullable=false) private String desParentesco;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="id_cie10") private PamcCie10 cie10;
    @Column(name="des_padecimiento") private String desPadecimiento;
    @Column(name="des_observaciones",columnDefinition="TEXT") private String desObservaciones;
    @Column(name="stp_alta") private Instant stpAlta;
}
