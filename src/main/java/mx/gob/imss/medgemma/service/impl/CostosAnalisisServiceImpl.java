package mx.gob.imss.medgemma.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.AnalizarNotasRequest;
import mx.gob.imss.medgemma.dto.response.AnalizarNotasResponse;
import mx.gob.imss.medgemma.dto.response.ConsultaBriefDto;
import mx.gob.imss.medgemma.dto.response.ProcedimientoDetectadoDto;
import mx.gob.imss.medgemma.entity.PamcCostoProcedimiento;
import mx.gob.imss.medgemma.entity.PamtConsulta;
import mx.gob.imss.medgemma.repository.PamcCostoProcedimientoRepository;
import mx.gob.imss.medgemma.repository.PamtConsultaRepository;
import mx.gob.imss.medgemma.service.CostosAnalisisService;
import mx.gob.imss.medgemma.service.LmStudioService;
import mx.gob.imss.medgemma.service.ProcedimientoDetectorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostosAnalisisServiceImpl implements CostosAnalisisService {

    private final PamtConsultaRepository           consultaRepo;
    private final PamcCostoProcedimientoRepository procedimientoRepo;
    private final ProcedimientoDetectorService     detector;
    private final LmStudioService                  lmStudioService;

    private static final List<String> PROCS_POR_DIA =
            List.of("DIAS_HOSPITALIZACION", "DIAS_CUNERO", "DIAS_VENTILADOR");

    @Override
    public List<ConsultaBriefDto> obtenerConsultasPaciente(String curp) {
        log.info("Obteniendo consultas para CURP: {}", curp);
        return consultaRepo.findByCurp(curp.toUpperCase())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public AnalizarNotasResponse analizarNotas(AnalizarNotasRequest request) {
        log.info("Analizando notas — CURP: {} | archivo: {} chars",
                request.getCurp(),
                request.getNotaTexto() != null ? request.getNotaTexto().length() : 0);

        StringBuilder textoTotal = new StringBuilder();

        if (request.getCurp() != null && !request.getCurp().isBlank()) {
            List<PamtConsulta> consultas = consultaRepo.findByCurp(request.getCurp().toUpperCase());

            if (request.getConsultaIds() != null && !request.getConsultaIds().isEmpty()) {
                Set<String> ids = new HashSet<>(request.getConsultaIds());
                consultas = consultas.stream()
                        .filter(c -> ids.contains(c.getIdConsulta().toString()))
                        .collect(Collectors.toList());
            }

            consultas.forEach(c -> textoTotal.append(buildTextoConsulta(c)).append("\n\n"));
        }

        if (request.getNotaTexto() != null && !request.getNotaTexto().isBlank()) {
            textoTotal.append(request.getNotaTexto());
        }

        List<PamcCostoProcedimiento> catalogo = procedimientoRepo.findByIndActivoTrue();
        String textoFinal = textoTotal.toString();

        // ── Detección en paralelo: keywords y MedGemma corren al mismo tiempo
        long t0 = System.currentTimeMillis();

        CompletableFuture<List<ProcedimientoDetectadoDto>> futureKeywords =
                CompletableFuture.supplyAsync(() -> detector.detectar(textoFinal, catalogo));

        CompletableFuture<List<ProcedimientoDetectadoDto>> futureLLM =
                CompletableFuture.supplyAsync(() -> detectarConLLM(textoFinal, catalogo));

        List<ProcedimientoDetectadoDto> porKeywords = futureKeywords.join();
        List<ProcedimientoDetectadoDto> porLLM      = futureLLM.join();

        // ── Fusión: keywords + lo que solo detectó el LLM (sin duplicar por CVE)
        Set<String> cvesYaDetectados = porKeywords.stream()
                .map(ProcedimientoDetectadoDto::getCveProcedimiento)
                .collect(Collectors.toSet());

        List<ProcedimientoDetectadoDto> procedimientos = new ArrayList<>(porKeywords);
        porLLM.stream()
                .filter(p -> !cvesYaDetectados.contains(p.getCveProcedimiento()))
                .forEach(procedimientos::add);

        log.info("Detección paralela — keywords: {} | LLM: {} | fusionados: {} | {}ms",
                porKeywords.size(), porLLM.size(), procedimientos.size(),
                System.currentTimeMillis() - t0);

        long diasHosp = calcularDias(request);

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal total1er  = BigDecimal.ZERO;
        BigDecimal total2do  = BigDecimal.ZERO;
        BigDecimal total3er  = BigDecimal.ZERO;

        for (ProcedimientoDetectadoDto p : procedimientos) {
            BigDecimal mult = BigDecimal.valueOf(
                    esPorDia(p.getCveProcedimiento()) && diasHosp > 0 ? diasHosp : 1);

            totalBase = totalBase.add(p.getNumCostoBase().multiply(mult));
            if (p.getNumCosto1erNivel() != null)
                total1er = total1er.add(p.getNumCosto1erNivel().multiply(mult));
            if (p.getNumCosto2doNivel() != null)
                total2do = total2do.add(p.getNumCosto2doNivel().multiply(mult));
            if (p.getNumCosto3erNivel() != null)
                total3er = total3er.add(p.getNumCosto3erNivel().multiply(mult));
        }

        log.info("Análisis completo — {} procedimientos | {} días | total base: ${}",
                procedimientos.size(), diasHosp, String.format("%,.2f", totalBase));

        return AnalizarNotasResponse.builder()
                .procedimientosDetectados(procedimientos)
                .diasHospitalizacion(diasHosp)
                .totalBase(totalBase)
                .total1erNivel(total1er)
                .total2doNivel(total2do)
                .total3erNivel(total3er)
                .totalProcedimientos(procedimientos.size())
                .build();
    }

    private ConsultaBriefDto toDto(PamtConsulta c) {
        String fecha = c.getFecConsulta() != null
                ? c.getFecConsulta().atZone(ZoneId.of("America/Mexico_City"))
                        .toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";

        String medico = c.getMedico() != null
                ? (c.getMedico().getNomNombre() + " " + c.getMedico().getNomPrimerApellido()).trim()
                : "—";

        return ConsultaBriefDto.builder()
                .idConsulta(c.getIdConsulta().toString())
                .fecConsulta(fecha)
                .desTipoConsulta(c.getDesTipoConsulta())
                .desMotivoConsulta(c.getDesMotivoConsulta())
                .nombreMedico(medico)
                .textoNota(buildTextoConsulta(c))
                .build();
    }

    private String buildTextoConsulta(PamtConsulta c) {
        StringBuilder sb = new StringBuilder();
        if (c.getDesMotivoConsulta() != null) sb.append(c.getDesMotivoConsulta()).append(" ");
        if (c.getDesSubjetivo()      != null) sb.append(c.getDesSubjetivo()).append(" ");
        if (c.getDesObjetivo()       != null) sb.append(c.getDesObjetivo()).append(" ");
        if (c.getDesAnalisis()       != null) sb.append(c.getDesAnalisis()).append(" ");
        if (c.getDesPlan()           != null) sb.append(c.getDesPlan()).append(" ");
        return sb.toString().trim();
    }

    private long calcularDias(AnalizarNotasRequest req) {
        try {
            if (req.getFechaIngreso() != null && req.getFechaEgreso() != null
                    && !req.getFechaIngreso().isBlank() && !req.getFechaEgreso().isBlank()) {
                return Math.max(
                        ChronoUnit.DAYS.between(
                                LocalDate.parse(req.getFechaIngreso()),
                                LocalDate.parse(req.getFechaEgreso())) + 1,
                        0);
            }
        } catch (Exception e) {
            log.warn("No se pudo calcular días de hospitalización: {}", e.getMessage());
        }
        return 0;
    }

    private boolean esPorDia(String cve) {
        return PROCS_POR_DIA.contains(cve);
    }

    // ── DETECCIÓN CON LLM ──────────────────────────────────────────────────────

    private List<ProcedimientoDetectadoDto> detectarConLLM(String texto, List<PamcCostoProcedimiento> catalogo) {
        if (texto == null || texto.isBlank()) return List.of();

        String catalogoTexto = catalogo.stream()
                .map(p -> p.getCveProcedimiento() + "|" + p.getDesProcedimiento())
                .collect(Collectors.joining("\n"));

        String textoNota = texto.length() > 3500 ? texto.substring(0, 3500) + "\n[...truncado...]" : texto;

        String prompt = "Eres un asistente médico del IMSS. Analiza la siguiente nota médica e identifica "
                + "qué procedimientos del catálogo están mencionados o claramente implicados en la nota.\n\n"
                + "CATÁLOGO (formato CVE|Descripción):\n" + catalogoTexto
                + "\n\nNOTA MÉDICA:\n" + textoNota
                + "\n\nResponde ÚNICAMENTE con las claves CVE de los procedimientos detectados, "
                + "una por línea, exactamente como aparecen en el catálogo. "
                + "Sin explicación, sin numeración, sin texto adicional. "
                + "Si no detectas ninguno, responde: NINGUNO";

        try {
            String respuesta = lmStudioService.chatSimple(prompt).getContent();
            log.info("LLM detección costos — respuesta: {}", respuesta.replace("\n", " | "));

            Map<String, PamcCostoProcedimiento> mapaC = catalogo.stream()
                    .collect(Collectors.toMap(PamcCostoProcedimiento::getCveProcedimiento, p -> p));

            return Arrays.stream(respuesta.split("\\R"))
                    .map(String::trim)
                    .filter(mapaC::containsKey)
                    .map(cve -> toDtoProcedimiento(mapaC.get(cve)))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("LLM no disponible para detección de costos, usando solo keywords: {}", e.getMessage());
            return List.of();
        }
    }

    private ProcedimientoDetectadoDto toDtoProcedimiento(PamcCostoProcedimiento p) {
        return ProcedimientoDetectadoDto.builder()
                .cveProcedimiento(p.getCveProcedimiento())
                .desProcedimiento(p.getDesProcedimiento())
                .numCostoBase(p.getNumCostoBase())
                .numCosto1erNivel(p.getNumCosto1erNivel())
                .numCosto2doNivel(p.getNumCosto2doNivel())
                .numCosto3erNivel(p.getNumCosto3erNivel())
                .build();
    }
}
