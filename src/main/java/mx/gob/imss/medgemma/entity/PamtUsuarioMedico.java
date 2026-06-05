package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;
@Data @Entity @Table(name="pamt_usuario_medico",schema="imss_ai")
public class PamtUsuarioMedico {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) @Column(name="id_usuario",columnDefinition="uuid") private UUID idUsuario;
    @Column(name="num_matricula",nullable=false,unique=true) private String numMatricula;
    @Column(name="num_cedula_prof",unique=true) private String numCedulaProf;
    @Column(name="nom_nombre",nullable=false) private String nomNombre;
    @Column(name="nom_primer_apellido",nullable=false) private String nomPrimerApellido;
    @Column(name="nom_segundo_apellido") private String nomSegundoApellido;
    @Column(name="des_email",unique=true) private String desEmail;
    @Column(name="num_telefono") private String numTelefono;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_especialidad") private PamcEspecialidad especialidad;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_unidad_medica") private PamcUnidadMedica unidadMedica;
    @Column(name="des_turno") private String desTurno;
    @Column(name="des_nivel") private String desNivel;
    @Column(name="des_password_hash",nullable=false) private String desPasswordHash;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="fec_ultimo_acceso") private Instant fecUltimoAcceso;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
    @Column(name="ref_usuario_alta") private String refUsuarioAlta;
}
