package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Data @Entity @Table(name="pamt_paciente",schema="imss_ai")
public class PamtPaciente {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) @Column(name="id_paciente",columnDefinition="uuid") private UUID idPaciente;
    @Column(name="cve_curp",nullable=false,unique=true,length=18) private String cveCurp;
    @Column(name="num_nss",unique=true) private String numNss;
    @Column(name="nom_nombre",nullable=false) private String nomNombre;
    @Column(name="nom_primer_apellido",nullable=false) private String nomPrimerApellido;
    @Column(name="nom_segundo_apellido") private String nomSegundoApellido;
    @Column(name="fec_nacimiento",nullable=false) private LocalDate fecNacimiento;
    @Column(name="des_sexo",nullable=false,length=1) private String desSexo;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_tipo_sangre") private PamcTipoSangre tipoSangre;
    @Column(name="des_nacionalidad") private String desNacionalidad;
    @Column(name="num_telefono") private String numTelefono;
    @Column(name="des_email") private String desEmail;
    @Column(name="des_direccion") private String desDireccion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_unidad_adscripcion") private PamcUnidadMedica unidadAdscripcion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ref_medico_familiar") private PamtUsuarioMedico medicoFamiliar;
    @Column(name="des_turno") private String desTurno;
    @Column(name="nom_contacto_emergencia") private String nomContactoEmergencia;
    @Column(name="num_telefono_emergencia") private String numTelefonoEmergencia;
    @Column(name="des_parentesco_emergencia") private String desParentescoEmergencia;
    @Column(name="ind_activo") private Boolean indActivo=true;
    @Column(name="stp_alta") private Instant stpAlta;
    @Column(name="stp_actualizacion") private Instant stpActualizacion;
    @Column(name="ref_usuario_alta") private String refUsuarioAlta;
}
