package mx.gob.imss.medgemma.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.builder.SystemPromptBuilder;
import mx.gob.imss.medgemma.client.LmStudioClient;
import mx.gob.imss.medgemma.config.LmStudioConfig;
import mx.gob.imss.medgemma.dto.contexto.PacienteContextoDto;
import mx.gob.imss.medgemma.dto.request.ImssAiChatRequest;
import mx.gob.imss.medgemma.dto.request.LmStudioChatRequest;
import mx.gob.imss.medgemma.dto.response.ImssAiChatResponse;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
import mx.gob.imss.medgemma.dto.response.ProcedimientoDetectadoDto;
import mx.gob.imss.medgemma.entity.PamcCostoProcedimiento;
import mx.gob.imss.medgemma.entity.PamtRagConsultaLog;
import mx.gob.imss.medgemma.repository.PamcCostoProcedimientoRepository;
import mx.gob.imss.medgemma.repository.PamtRagConsultaLogRepository;
import mx.gob.imss.medgemma.service.ImssAiContextService;
import mx.gob.imss.medgemma.service.PacienteContextoService;
import mx.gob.imss.medgemma.service.ProcedimientoDetectorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImssAiContextServiceImpl implements ImssAiContextService {

	private final PacienteContextoService              contextoService;
	private final LmStudioClient                       lmStudioClient;
	private final LmStudioConfig                       config;
	private final PamtRagConsultaLogRepository         logRepo;
	private final PamcCostoProcedimientoRepository     procedimientoRepo;
	private final ProcedimientoDetectorService         detector;

	@Override
	public ImssAiChatResponse chatConContexto(ImssAiChatRequest request) {
		log.info("chatConContexto — CURP: {} | Médico: {} | Especialidad: {}", request.getCurp(),
				request.getNumMatricula(), request.getEspecialidadMedico());

		boolean tieneContexto = request.getCurp() != null && !request.getCurp().isBlank();

		// ── 1. Resolver especialidad
		String especialidad = tieneEspecialidadMedico(request) ? request.getEspecialidadMedico()
				: SystemPromptBuilder.detectarEspecialidad(request.getPregunta(), 0);

		// ── 2. Construir prompt según modo
		String promptFinal;

		// ── Detección de procedimientos en la nota médica
		List<PamcCostoProcedimiento> catalogo = procedimientoRepo.findByIndActivoTrue();
		List<ProcedimientoDetectadoDto> procedimientosDetectados =
				detector.detectar(request.getNotaMedica(), catalogo);

		// ── Calcular días y total UNA SOLA VEZ en el backend
		long diasHosp          = calcularDiasHospitalizacion(request);
		BigDecimal totalBackend = calcularTotalProcedimientos(procedimientosDetectados, diasHosp);

		String bloqueNota = construirBloqueNota(request, procedimientosDetectados, diasHosp, totalBackend);

		boolean hayProcedimientos = !procedimientosDetectados.isEmpty();
		String formatoRespuesta   = hayProcedimientos
				? construirFormatoCostos(totalBackend)
				: "Responde en español médico formal siguiendo el formato indicado.";

		if (tieneContexto) {
			PacienteContextoDto contexto = contextoService.ensamblarContexto(request.getCurp());
			log.info("Contexto ensamblado — paciente: {} | secciones: {} | especialidad: {}",
					contexto.getNombreCompleto(), contarSecciones(contexto), especialidad);

			promptFinal = SystemPromptBuilder.build(especialidad)
					+ "\n\n" + contexto.toPromptText()
					+ bloqueNota
					+ "\n\n══ CONSULTA DEL MÉDICO ══\n" + request.getPregunta()
					+ "\n\n" + formatoRespuesta;

		} else {
			log.info("Modo consulta general — sin paciente | especialidad: {}", especialidad);

			// La nota va ANTES del rol general para que sea el contexto principal
			promptFinal = bloqueNota.isBlank()
					? SystemPromptBuilder.build(especialidad)
						+ "\n\nConsulta general de la especialidad."
						+ "\n\n══ CONSULTA DEL MÉDICO ══\n" + request.getPregunta()
						+ "\n\n" + formatoRespuesta
					: SystemPromptBuilder.build(especialidad)
						+ bloqueNota
						+ "\n\n══ CONSULTA DEL MÉDICO SOBRE LA NOTA ANTERIOR ══\n" + request.getPregunta()
						+ "\n\n" + formatoRespuesta;
		}

		log.info("Prompt final — {} caracteres | modo: {}", promptFinal.length(),
				tieneContexto ? "con paciente" : "general");

		// ── 3. Llamar a MedGemma via LM Studio
		LmStudioChatRequest lmRequest = LmStudioChatRequest.builder()
				.model(request.getModelo() != null ? request.getModelo() : config.getDefaultModel()).input(promptFinal)
				.contextLength(config.getContextLength()).temperature(config.getTemperature()).build();

		LmStudioChatResponse lmResponse = lmStudioClient.chat(lmRequest);

		log.info("Respuesta — tokens: {} | tok/s: {} | especialidad: {}",
				lmResponse.getStats() != null ? lmResponse.getStats().getTotalOutputTokens() : "N/A",
				lmResponse.getStats() != null ? lmResponse.getStats().getTokensPerSecond() : "N/A", especialidad);

		// ── 4. Guardar log de auditoría
		guardarLog(request, lmResponse, especialidad);

		// ── 5. Retornar response
		return ImssAiChatResponse.builder()
				.respuesta(lmResponse.getContent())
				.responseId(lmResponse.getResponseId())
				.modelInstanceId(lmResponse.getModelInstanceId())
				.totalOutputTokens(lmResponse.getStats() != null ? lmResponse.getStats().getTotalOutputTokens() : null)
				.tokensPerSecond(lmResponse.getStats() != null ? lmResponse.getStats().getTokensPerSecond() : null)
				.curpPaciente(request.getCurp())
				.nombrePaciente(tieneContexto ? "cargado desde Neon" : "consulta general")
				.contextoEnriquecido(tieneContexto)
				.seccionesContexto(tieneContexto ? 0 : 0)
				.especialidadDetectada(especialidad)
				.procedimientosDetectados(procedimientosDetectados)
				.build();
	}

	// ─────────────────────────────────────────────────────
	// MÉTODOS PRIVADOS
	// ─────────────────────────────────────────────────────

	private static final Pattern PATRON_FECHAS_ESTANCIA = Pattern.compile(
			"del\\s+(\\d{1,2})\\s+de\\s+(\\w+)\\s+del?\\s+(\\d{4})\\s+al\\s+(\\d{1,2})\\s+de\\s+(\\w+)\\s+del?\\s+(\\d{4})",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Map<String, Integer> MESES_ES = Map.ofEntries(
			Map.entry("enero", 1),   Map.entry("febrero", 2),  Map.entry("marzo", 3),
			Map.entry("abril", 4),   Map.entry("mayo", 5),     Map.entry("junio", 6),
			Map.entry("julio", 7),   Map.entry("agosto", 8),   Map.entry("septiembre", 9),
			Map.entry("octubre", 10),Map.entry("noviembre", 11),Map.entry("diciembre", 12));

	/** Calcula los días entre fechaIngreso y fechaEgreso del request.
	 *  Si las fechas explícitas no se envían, intenta extraerlas del texto de la nota. */
	private long calcularDiasHospitalizacion(ImssAiChatRequest req) {
		try {
			if (req.getFechaIngreso() != null && req.getFechaEgreso() != null
					&& !req.getFechaIngreso().isBlank() && !req.getFechaEgreso().isBlank()) {
				long dias = ChronoUnit.DAYS.between(
						LocalDate.parse(req.getFechaIngreso()),
						LocalDate.parse(req.getFechaEgreso()));
				// +1 para contar ambos días (ingreso y egreso) de forma inclusiva
				return Math.max(dias + 1, 0);
			}
			if (req.getNotaMedica() != null && !req.getNotaMedica().isBlank()) {
				return extraerDiasDeNota(req.getNotaMedica());
			}
		} catch (Exception e) {
			log.warn("No se pudo calcular días de hospitalización: {}", e.getMessage());
		}
		return 0;
	}

	private long extraerDiasDeNota(String nota) {
		Matcher m = PATRON_FECHAS_ESTANCIA.matcher(nota);
		if (!m.find()) return 0;
		LocalDate ingreso = parseFechaEspanol(m.group(1), m.group(2), m.group(3));
		LocalDate egreso  = parseFechaEspanol(m.group(4), m.group(5), m.group(6));
		if (ingreso == null || egreso == null) return 0;
		long dias = ChronoUnit.DAYS.between(ingreso, egreso) + 1; // inclusivo
		log.info("Fechas extraídas de la nota — ingreso: {} | egreso: {} | días: {}", ingreso, egreso, dias);
		return Math.max(dias, 0);
	}

	private LocalDate parseFechaEspanol(String dia, String mes, String anio) {
		Integer numMes = MESES_ES.get(mes.toLowerCase());
		if (numMes == null) return null;
		return LocalDate.of(Integer.parseInt(anio), numMes, Integer.parseInt(dia));
	}

	/** Suma los costos efectivos de cada procedimiento (por día × días si aplica). */
	private BigDecimal calcularTotalProcedimientos(List<ProcedimientoDetectadoDto> procs, long diasHosp) {
		BigDecimal total = BigDecimal.ZERO;
		for (ProcedimientoDetectadoDto p : procs) {
			BigDecimal costo = esProcedimientoPorDia(p.getCveProcedimiento()) && diasHosp > 0
					? p.getNumCostoBase().multiply(BigDecimal.valueOf(diasHosp))
					: p.getNumCostoBase();
			total = total.add(costo);
		}
		return total;
	}

	private boolean esProcedimientoPorDia(String cve) {
		return List.of("DIAS_HOSPITALIZACION", "DIAS_CUNERO", "DIAS_VENTILADOR").contains(cve);
	}

	private String construirBloqueNota(ImssAiChatRequest req,
			List<ProcedimientoDetectadoDto> procedimientos,
			long diasHosp, BigDecimal totalCalculado) {

		boolean tieneNota    = req.getNotaMedica()   != null && !req.getNotaMedica().isBlank();
		boolean tieneIngreso = req.getFechaIngreso() != null && !req.getFechaIngreso().isBlank();
		boolean tieneEgreso  = req.getFechaEgreso()  != null && !req.getFechaEgreso().isBlank();
		boolean tieneProcs   = procedimientos         != null && !procedimientos.isEmpty();

		if (!tieneNota && !tieneIngreso && !tieneEgreso) return "";

		StringBuilder sb = new StringBuilder("\n\n=== NOTA DE HOSPITALIZACIÓN (DATOS REALES — NO RECALCULAR) ===\n");

		if (tieneIngreso) sb.append("Fecha de ingreso : ").append(req.getFechaIngreso()).append("\n");
		if (tieneEgreso)  sb.append("Fecha de egreso  : ").append(req.getFechaEgreso()).append("\n");
		if (diasHosp > 0) sb.append("Días de estancia : ").append(diasHosp).append(" día(s)\n");

		if (tieneNota) {
			sb.append("\nContenido de la nota médica:\n");
			String nota = req.getNotaMedica();
			sb.append(nota.length() > 4000 ? nota.substring(0, 4000) + "\n[...nota truncada...]" : nota);
		}

		if (tieneProcs) {
			sb.append("\n\n--- DESGLOSE DE COSTOS (YA CALCULADOS POR EL SISTEMA) ---\n");
			sb.append("⚠ ESTOS VALORES SON DEFINITIVOS. NO los recalcules. Cítalos tal como aparecen.\n\n");

			for (ProcedimientoDetectadoDto p : procedimientos) {
				BigDecimal costoEfectivo = esProcedimientoPorDia(p.getCveProcedimiento()) && diasHosp > 0
						? p.getNumCostoBase().multiply(BigDecimal.valueOf(diasHosp))
						: p.getNumCostoBase();

				if (esProcedimientoPorDia(p.getCveProcedimiento()) && diasHosp > 0) {
					sb.append(String.format("• %-30s  $%,12.2f  (= $%,.2f × %d días)%n",
							p.getDesProcedimiento(), costoEfectivo, p.getNumCostoBase(), diasHosp));
				} else {
					sb.append(String.format("• %-30s  $%,12.2f%n",
							p.getDesProcedimiento(), costoEfectivo));
				}

				sb.append("  Costos por nivel — ");
				sb.append(p.getNumCosto1erNivel() != null
						? String.format("UMF: $%,.2f  ", p.getNumCosto1erNivel()) : "UMF: N/A  ");
				sb.append(p.getNumCosto2doNivel() != null
						? String.format("HGZ: $%,.2f  ", p.getNumCosto2doNivel()) : "HGZ: N/A  ");
				sb.append(p.getNumCosto3erNivel() != null
						? String.format("UMAE: $%,.2f%n", p.getNumCosto3erNivel()) : "UMAE: N/A\n");
			}

			sb.append(String.format("%n════════════════════════════════════════%n"));
			sb.append(String.format("  TOTAL = $%,.2f MXN  ← USA ESTE VALOR EXACTO%n", totalCalculado));
			sb.append(String.format("════════════════════════════════════════%n"));
		}

		sb.append("=== FIN NOTA ===");

		log.info("Bloque nota — ingreso: {} | egreso: {} | días: {} | procedimientos: {} | total: ${}",
				req.getFechaIngreso(), req.getFechaEgreso(), diasHosp,
				tieneProcs ? procedimientos.size() : 0,
				String.format("%,.2f", totalCalculado));

		return sb.toString();
	}

	private String construirFormatoCostos(BigDecimal totalCalculado) {
		String totalStr = String.format("$%,.2f MXN", totalCalculado);
		return "Responde EXCLUSIVAMENTE con el siguiente formato. "
			+ "El total ya fue calculado correctamente por el sistema: " + totalStr + ". "
			+ "NO hagas ningún cálculo aritmético propio. Copia los valores exactamente como aparecen.\n\n"
			+ "**RESUMEN DE LA HOSPITALIZACIÓN:**\n"
			+ "(Fecha de ingreso, fecha de egreso y días de estancia — copia los valores exactos)\n\n"
			+ "**PROCEDIMIENTOS Y COSTOS:**\n"
			+ "(Lista cada procedimiento con su costo tal como aparece en el desglose. "
			+ "Para procedimientos por día muestra también el cálculo unitario × días)\n\n"
			+ "**COSTO TOTAL: " + totalStr + "**\n"
			+ "(Este valor es definitivo. No lo modifiques ni lo recalcules)\n\n"
			+ "**NIVEL DE ATENCIÓN:**\n"
			+ "(Indica si los costos corresponden a UMF, HGZ/HGR o UMAE y por qué)\n\n"
			+ "**OBSERVACIONES CLÍNICAS:**\n"
			+ "(Máximo 2 líneas sobre el episodio hospitalario desde perspectiva clínica)";
	}

	private boolean tieneEspecialidadMedico(ImssAiChatRequest request) {
		return request.getEspecialidadMedico() != null && !request.getEspecialidadMedico().isBlank();
	}

	private int contarSecciones(PacienteContextoDto c) {
		int s = 1;
		if (c.getAlergias() != null && !c.getAlergias().isEmpty())
			s++;
		if (c.getAntecedentesPatologicos() != null && !c.getAntecedentesPatologicos().isEmpty())
			s++;
		if (c.getAntecedentesHeredofamiliares() != null && !c.getAntecedentesHeredofamiliares().isEmpty())
			s++;
		if (c.getAntecedentesNoPatologicos() != null)
			s++;
		if (c.getUltimasConsultas() != null && !c.getUltimasConsultas().isEmpty())
			s++;
		if (c.getVacunasAplicadas() != null && !c.getVacunasAplicadas().isEmpty())
			s++;
		return s;
	}

	private void guardarLog(ImssAiChatRequest req, LmStudioChatResponse res, String especialidad) {
		try {
			PamtRagConsultaLog entry = new PamtRagConsultaLog();
			entry.setDesQuery(req.getPregunta() + " [" + especialidad + "]");
			entry.setDesRespuesta(res.getContent());
			entry.setDesModelo(config.getDefaultModel());
			entry.setStpConsulta(Instant.now());
			if (res.getStats() != null) {
				entry.setNumTokensSalida(res.getStats().getTotalOutputTokens());
				if (res.getStats().getTokensPerSecond() != null)
					entry.setNumTokensPorSeg(BigDecimal.valueOf(res.getStats().getTokensPerSecond()));
			}
			logRepo.save(entry);
		} catch (Exception e) {
			log.warn("No se pudo guardar log RAG: {}", e.getMessage());
		}
	}
}