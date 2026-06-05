package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
@Data @Entity @Table(name="pamt_antecedente_no_patologico",schema="imss_ai")
public class PamtAntecedenteNoPatologico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_antecedente") private Integer idAntecedente;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false,unique=true) private PamtExpedienteClinico expediente;
    @Column(name="des_tabaquismo") private String desTabaquismo;
    @Column(name="num_cigarros_dia") private Integer numCigarrosDia;
    @Column(name="des_alcoholismo") private String desAlcoholismo;
    @Column(name="des_actividad_fisica") private String desActividadFisica;
    @Column(name="des_alimentacion",columnDefinition="TEXT") private String desAlimentacion;
    @Column(name="des_ocupacion") private String desOcupacion;
    @Column(name="des_escolaridad") private String desEscolaridad;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
}
