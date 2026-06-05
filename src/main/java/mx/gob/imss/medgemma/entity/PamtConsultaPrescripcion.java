package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamt_consulta_prescripcion",schema="imss_ai")
public class PamtConsultaPrescripcion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_prescripcion") private Integer idPrescripcion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_consulta",nullable=false) private PamtConsulta consulta;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="id_medicamento",nullable=false) private PamcMedicamento medicamento;
    @Column(name="des_dosis",nullable=false) private String desDosis;
    @Column(name="des_frecuencia",nullable=false) private String desFrecuencia;
    @Column(name="des_via") private String desVia;
    @Column(name="num_duracion_dias") private Integer numDuracionDias;
    @Column(name="des_indicaciones",columnDefinition="TEXT") private String desIndicaciones;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
}
