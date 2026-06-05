package mx.gob.imss.medgemma.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImssAiChatResponse {

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

	/** CURP del paciente cuyo contexto se usó */
	private String curpPaciente;

	/** Nombre completo del paciente para mostrar en la UI */
	private String nombrePaciente;

	/** Indica si se usó el contexto clínico completo del paciente */
	private Boolean contextoEnriquecido;

	/** Número de secciones del contexto clínico incluidas en el prompt */
	private Integer seccionesContexto;

	/**
	 * Especialidad médica detectada automáticamente para esta consulta. Valores
	 * posibles: MEDICINA_GENERAL, ENDOCRINOLOGIA, CARDIOLOGIA, PEDIATRIA,
	 * GINECOLOGIA
	 */
	private String especialidadDetectada;

	/** Procedimientos del catálogo IMSS detectados en la nota médica adjunta */
	private List<ProcedimientoDetectadoDto> procedimientosDetectados;
}
