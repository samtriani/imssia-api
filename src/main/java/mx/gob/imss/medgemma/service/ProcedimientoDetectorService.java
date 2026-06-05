package mx.gob.imss.medgemma.service;

import mx.gob.imss.medgemma.dto.response.ProcedimientoDetectadoDto;
import mx.gob.imss.medgemma.entity.PamcCostoProcedimiento;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProcedimientoDetectorService {

    // Palabras clave por cve_procedimiento para mejorar la detección en texto clínico
    private static final Map<String, List<String>> KEYWORDS = Map.ofEntries(
        Map.entry("CONSULTA_GENERAL",        List.of("consulta general", "consulta medica", "medicina general")),
        Map.entry("CURACION_NO_DENTAL",      List.of("curacion", "curación de herida", "manejo de herida", "cura ")),
        Map.entry("CONSULTA_DENTAL",         List.of("consulta dental", "odontologia", "odontologica")),
        Map.entry("OBTURACION_EXTRACCION",   List.of("obturacion", "extraccion dental", "exodoncia", "empaste")),
        Map.entry("DETECCIONES_SIFILIS_VIH", List.of("sifilis", "vih", "prueba rapida", "deteccion")),
        Map.entry("CITOLOGIAS",              List.of("citologia", "papanicolaou", "pap ", "cervicovaginal", "colposcopia")),
        Map.entry("CONSULTA_ESPECIALIDAD",   List.of("especialidad", "especialista", "consulta especializada")),
        Map.entry("URGENCIAS",               List.of("urgencias", "urgencia", "servicio de urgencias", "area de urgencias")),
        Map.entry("DIAS_HOSPITALIZACION",    List.of("hospitalizacion", "internamiento", "dias de estancia", "dias cama", "ingreso hospitalario")),
        Map.entry("DIAS_CUNERO",             List.of("cunero", "cunas")),
        Map.entry("DIAS_VENTILADOR",         List.of("ventilador", "ventilacion mecanica", "intubacion", "soporte ventilatorio")),
        Map.entry("TOCOCIRUGIA",             List.of("tococirugia", "tocoquirurgica")),
        Map.entry("PARTO",                   List.of("parto", "nacimiento", "alumbramiento", "labor de parto", "expulsion fetal")),
        Map.entry("CESAREA",                 List.of("cesarea", "operacion cesarea", "intervencion cesarea")),
        Map.entry("ABORTO",                  List.of("aborto", "interrupcion del embarazo", "perdida gestacional")),
        Map.entry("RECIEN_NACIDO",           List.of("recien nacido", "neonato", "neonatal", "rn ")),
        Map.entry("LABORATORIO",             List.of("laboratorio", "biometria hematica", "quimica sanguinea", "analisis", "muestra sanguinea")),
        Map.entry("RAYOS_X",                 List.of("rayos x", "radiografia", "placa ", "rx ")),
        Map.entry("ULTRASONIDOS",            List.of("ultrasonido", "ecografia", "usg", "eco ", "sonografia")),
        Map.entry("ELECTROCARDIOGRAMAS",     List.of("electrocardiograma", "ecg", "ekg", "trazo electrocardiografico")),
        Map.entry("TRASLADO_AMBULANCIA",     List.of("ambulancia", "traslado", "referencia medica", "contrarreferencia"))
    );

    /**
     * Detecta procedimientos del catálogo mencionados en el texto de la nota médica.
     * Aplica normalización Unicode (sin acentos) y comparación case-insensitive.
     */
    public List<ProcedimientoDetectadoDto> detectar(String notaMedica, List<PamcCostoProcedimiento> catalogo) {
        if (notaMedica == null || notaMedica.isBlank()) return List.of();

        String textoNorm = normalizar(notaMedica);
        List<ProcedimientoDetectadoDto> detectados = new ArrayList<>();

        for (PamcCostoProcedimiento proc : catalogo) {
            if (coincide(textoNorm, proc)) {
                detectados.add(toDto(proc));
            }
        }

        return detectados;
    }

    private boolean coincide(String textoNorm, PamcCostoProcedimiento proc) {
        // 1. Descripción normalizada
        if (textoNorm.contains(normalizar(proc.getDesProcedimiento()))) return true;

        // 2. Clave con underscores → espacios
        String cveNorm = proc.getCveProcedimiento().toLowerCase().replace("_", " ");
        if (textoNorm.contains(cveNorm)) return true;

        // 3. Keywords clínicas por procedimiento
        List<String> kws = KEYWORDS.getOrDefault(proc.getCveProcedimiento(), List.of());
        return kws.stream().anyMatch(textoNorm::contains);
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase()
            .trim();
    }

    private ProcedimientoDetectadoDto toDto(PamcCostoProcedimiento p) {
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
