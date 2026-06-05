package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;
@Data @Entity @Table(name="pamt_consulta_diagnostico",schema="imss_ai")
public class PamtConsultaDiagnostico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_consulta_dx") private Integer idConsultaDx;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_consulta",nullable=false) private PamtConsulta consulta;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="id_cie10",nullable=false) private PamcCie10 cie10;
    @Column(name="des_tipo") private String desTipo;
    @Column(name="des_observaciones",columnDefinition="TEXT") private String desObservaciones;
    @Column(name="stp_alta") private Instant stpAlta;
}
