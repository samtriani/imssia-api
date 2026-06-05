package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
@Data @Entity @Table(name="pamt_antecedente_patologico",schema="imss_ai")
public class PamtAntecedentePatologico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_antecedente") private Integer idAntecedente;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false) private PamtExpedienteClinico expediente;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="id_cie10") private PamcCie10 cie10;
    @Column(name="des_padecimiento",nullable=false) private String desPadecimiento;
    @Column(name="fec_inicio") private LocalDate fecInicio;
    @Column(name="des_tratamiento_actual",columnDefinition="TEXT") private String desTratamientoActual;
    @Column(name="ind_cronico") private Boolean indCronico=false;
    @Column(name="ind_controlado") private Boolean indControlado;
    @Column(name="des_observaciones",columnDefinition="TEXT") private String desObservaciones;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
}
