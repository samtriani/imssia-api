package mx.gob.imss.medgemma.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data @Entity @Table(name="pamt_rag_consulta_log",schema="imss_ai")
public class PamtRagConsultaLog {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) @Column(name="id_log",columnDefinition="uuid") private UUID idLog;
    @Column(name="id_usuario",columnDefinition="uuid") private UUID idUsuario;
    @Column(name="id_paciente",columnDefinition="uuid") private UUID idPaciente;
    @Column(name="des_query",nullable=false,columnDefinition="TEXT") private String desQuery;
    @Column(name="des_respuesta",columnDefinition="TEXT") private String desRespuesta;
    @Column(name="num_chunks_usados") private Integer numChunksUsados;
    @Column(name="num_tokens_entrada") private Integer numTokensEntrada;
    @Column(name="num_tokens_salida") private Integer numTokensSalida;
    @Column(name="num_tokens_por_seg") private BigDecimal numTokensPorSeg;
    @Column(name="des_modelo") private String desModelo="medgemma-4b-it";
    @Column(name="stp_consulta",nullable=false) private Instant stpConsulta;
}
