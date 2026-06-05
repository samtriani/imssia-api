package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
@Data @Entity @Table(name="pamt_vacuna_aplicada",schema="imss_ai")
public class PamtVacunaAplicada {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_vacuna_aplicada") private Integer idVacunaAplicada;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false) private PamtExpedienteClinico expediente;
    @Column(name="des_vacuna",nullable=false) private String desVacuna;
    @Column(name="des_lote") private String desLote;
    @Column(name="num_dosis") private Integer numDosis;
    @Column(name="fec_aplicacion",nullable=false) private LocalDate fecAplicacion;
    @Column(name="des_observaciones",columnDefinition="TEXT") private String desObservaciones;
    @Column(name="stp_alta") private Instant stpAlta;
}
