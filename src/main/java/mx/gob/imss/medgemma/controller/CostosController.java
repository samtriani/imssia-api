package mx.gob.imss.medgemma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.request.AnalizarNotasRequest;
import mx.gob.imss.medgemma.dto.response.AnalizarNotasResponse;
import mx.gob.imss.medgemma.dto.response.ConsultaBriefDto;
import mx.gob.imss.medgemma.service.CostosAnalisisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/costos")
@RequiredArgsConstructor
@Tag(name = "Costos de Procedimientos", description = "Análisis de costos en notas médicas del paciente")
@CrossOrigin(origins = "*")
public class CostosController {

    private final CostosAnalisisService costosService;

    @PostMapping("/analizar")
    @Operation(summary = "Analizar notas médicas y detectar procedimientos con costos IMSS")
    public ResponseEntity<AnalizarNotasResponse> analizarNotas(@RequestBody AnalizarNotasRequest request) {
        log.info("POST /api/v1/costos/analizar — CURP: {} | consultaIds: {}",
                request.getCurp(), request.getConsultaIds());
        return ResponseEntity.ok(costosService.analizarNotas(request));
    }

    @GetMapping("/consultas/{curp}")
    @Operation(summary = "Obtener historial de consultas del paciente para análisis de costos")
    public ResponseEntity<List<ConsultaBriefDto>> getConsultas(@PathVariable String curp) {
        log.info("GET /api/v1/costos/consultas/{}", curp);
        return ResponseEntity.ok(costosService.obtenerConsultasPaciente(curp));
    }
}
