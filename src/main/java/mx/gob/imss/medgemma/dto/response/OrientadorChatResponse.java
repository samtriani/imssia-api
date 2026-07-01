package mx.gob.imss.medgemma.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrientadorChatResponse {

    /** Texto de respuesta generado por MedGemma */
    private String respuesta;

    /** ID único de la respuesta — generado por LM Studio */
    private String responseId;

    /** ID de la instancia del modelo en LM Studio (ej. "medgemma-4b-it:2") */
    private String modelInstanceId;

    /** Total de tokens generados en la respuesta */
    private Integer totalOutputTokens;

    /** Velocidad de generación en tokens por segundo */
    private Double tokensPerSecond;

    /** Tema de la guía ECSUS detectado a partir de la pregunta (puede ser nulo) */
    private String temaDetectado;

    /** Rutas (relativas a /assets) de las capturas de pantalla del tema detectado */
    private List<String> imagenesRelacionadas;

    /** URLs de los videos de guía del tema detectado */
    private List<String> videosRelacionados;
}
