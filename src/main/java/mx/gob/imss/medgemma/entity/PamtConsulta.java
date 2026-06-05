package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data @Entity @Table(name="pamt_consulta",schema="imss_ai")
public class PamtConsulta {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) @Column(name="id_consulta",columnDefinition="uuid") private UUID idConsulta;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_expediente",nullable=false) private PamtExpedienteClinico expediente;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_medico",nullable=false) private PamtUsuarioMedico medico;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_unidad_medica",nullable=false) private PamcUnidadMedica unidadMedica;
    @Column(name="fec_consulta",nullable=false) private Instant fecConsulta;
    @Column(name="des_tipo_consulta") private String desTipoConsulta;
    @Column(name="num_tension_sistolica") private Integer numTensionSistolica;
    @Column(name="num_tension_diastolica") private Integer numTensionDiastolica;
    @Column(name="num_frecuencia_cardiaca") private Integer numFrecuenciaCardiaca;
    @Column(name="num_frecuencia_resp") private Integer numFrecuenciaResp;
    @Column(name="num_temperatura") private BigDecimal numTemperatura;
    @Column(name="num_peso_kg") private BigDecimal numPesoKg;
    @Column(name="num_talla_cm") private BigDecimal numTallaCm;
    @Column(name="num_imc",insertable=false,updatable=false) private BigDecimal numImc;
    @Column(name="num_saturacion_o2") private Integer numSaturacionO2;
    @Column(name="num_glucosa_capilar") private BigDecimal numGlucosaCapilar;
    @Column(name="des_motivo_consulta",columnDefinition="TEXT") private String desMotivoConsulta;
    @Column(name="des_subjetivo",columnDefinition="TEXT") private String desSubjetivo;
    @Column(name="des_objetivo",columnDefinition="TEXT") private String desObjetivo;
    @Column(name="des_analisis",columnDefinition="TEXT") private String desAnalisis;
    @Column(name="des_plan",columnDefinition="TEXT") private String desPlan;
    @Column(name="des_pronostico") private String desPronostico;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="ref_usuario_alta") private String refUsuarioAlta;
}
