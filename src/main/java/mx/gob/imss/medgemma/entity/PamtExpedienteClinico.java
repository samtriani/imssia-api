package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Data @Entity @Table(name="pamt_expediente_clinico",schema="imss_ai")
public class PamtExpedienteClinico {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) @Column(name="id_expediente",columnDefinition="uuid") private UUID idExpediente;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_paciente",nullable=false,unique=true) private PamtPaciente paciente;
    @Column(name="fec_apertura",nullable=false) private LocalDate fecApertura;
    @Column(name="des_observaciones_generales") private String desObservacionesGenerales;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
    @Column(name="ref_usuario_alta") private String refUsuarioAlta;
}
