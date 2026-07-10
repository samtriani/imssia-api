package mx.gob.imss.medgemma.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.service.GuiaSistemaService;

/**
 * Carga las guías rápidas de ECSUS (Markdown) desde {@code classpath:guias/*.md}
 * y las mantiene en memoria para el Orientador del Sistema.
 */
@Slf4j
@Service
public class GuiaSistemaServiceImpl implements GuiaSistemaService {

    private static final Pattern PATRON_IMAGEN = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");
    private static final Pattern PATRON_TITULO = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);

    // Base fija de la bóveda documental; a cada URL solo se le concatena el fileId del video.
    private static final String VIDEO_BASE =
        "http://msbovedaimss-documentos.apps.qaocp.imss.gob.mx/api/files/dcdc2d3d-959b-4010-b93a-1547da7d176e/";

    // fileIds de los videos ECSUS (bóveda documental) — una constante por video, sin duplicar URL.
    private static final String V_ACCESO      = VIDEO_BASE + "f99e924d-e543-44f1-8067-39f4b7894423"; // ECSUS-1 Acceso y búsqueda
    private static final String V_AGENDAR     = VIDEO_BASE + "d3df4980-08f5-412d-87d4-b48841230da1"; // ECSUS-2 Agendar cita
    private static final String V_CONFIRMAR   = VIDEO_BASE + "97e638e6-c3d6-4cf0-911b-f9adc70bf4e6"; // ECSUS-3 Confirmar cita
    private static final String V_ATENCION    = VIDEO_BASE + "bbc8b3c4-4cf3-4c97-974e-3e1efb432569"; // ECSUS-4 Brindar atención médica
    private static final String V_RECETA_GRAL = VIDEO_BASE + "c3f8da68-c481-4fbe-972d-37ddc1d75748"; // ECSUS-6 Receta + laboratorio + otros
    private static final String V_RECETA      = VIDEO_BASE + "fecc61c4-e2cd-4e5a-9d5f-76c4c132870f"; // ECSUS-7 Generar receta médica
    private static final String V_LABORATORIO = VIDEO_BASE + "60411215-cb5a-47f7-aa03-62cced7bf4e4"; // ECSUS-8 Laboratorio
    private static final String V_RAYOS       = VIDEO_BASE + "526aff39-7d8a-4f99-8a7e-b30863a9ec4d"; // ECSUS-9 Rayos X
    private static final String V_HISTORIA    = VIDEO_BASE + "3b69d977-6929-4659-90c7-a392652ddbfd"; // ECSUS-10 Historia clínica

    // Videos por tema — vista general del tema; se usa como fallback cuando la pregunta no
    // coincide con una sub-categoría específica. Administrados aquí, nunca en los .md.
    private static final Map<String, List<String>> VIDEOS_POR_TEMA = Map.ofEntries(
        Map.entry("01-inicio-sesion",      List.of(V_ACCESO)),
        Map.entry("02-busqueda-pacientes", List.of(V_ACCESO)),
        Map.entry("03-historia-clinica",   List.of(V_HISTORIA)),
        Map.entry("04-agenda-citas",       List.of(V_AGENDAR, V_CONFIRMAR)),
        Map.entry("05-nota-medica",        List.of(V_ATENCION)),
        Map.entry("06-auxiliares-dx-tx",   List.of(V_RECETA_GRAL, V_RECETA, V_LABORATORIO, V_RAYOS))
    );

    // Selección FINA de videos por palabra clave (más específica que el tema de la guía).
    // Evita, p. ej., devolver los videos de laboratorio/rayos X cuando la pregunta es solo de receta.
    // El tema 06 (auxiliares dx/tx) agrupa 3 procedimientos; aquí se desglosan.
    private static final List<Map.Entry<List<String>, List<String>>> VIDEOS_POR_KEYWORD = List.of(
        Map.entry(List.of("receta", "medicamento", "prescrip", "imprimir receta"),
                  List.of(V_RECETA, V_RECETA_GRAL)),                              // ECSUS-7 (+ ECSUS-6 general)
        Map.entry(List.of("laboratorio", "solicitud de laboratorio", "análisis clínico", "analisis clinico"),
                  List.of(V_LABORATORIO, V_RECETA_GRAL)),                         // ECSUS-8 (+ ECSUS-6 general)
        Map.entry(List.of("rayos", "rayos x", "rayos equis", "radiograf", "imagenolog"),
                  List.of(V_RAYOS))                                              // ECSUS-9
    );

    private final Map<String, GuiaTema> temasPorClave = new LinkedHashMap<>();
    private String contextoGuias = "";

    @PostConstruct
    public void cargarGuias() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] recursos = resolver.getResources("classpath*:guias/*.md");

            List<Resource> ordenados = new ArrayList<>(List.of(recursos));
            ordenados.sort((a, b) -> a.getFilename().compareTo(b.getFilename()));

            StringBuilder contexto = new StringBuilder();
            for (Resource recurso : ordenados) {
                String original = StreamUtils.copyToString(recurso.getInputStream(), StandardCharsets.UTF_8);
                String clave = recurso.getFilename().replaceFirst("\\.md$", "");

                Matcher tituloMatcher = PATRON_TITULO.matcher(original);
                String titulo = tituloMatcher.find() ? tituloMatcher.group(1).trim() : clave;

                LinkedHashSet<String> imagenes = new LinkedHashSet<>();
                Matcher imagenMatcher = PATRON_IMAGEN.matcher(original);
                while (imagenMatcher.find()) {
                    imagenes.add(imagenMatcher.group(1).trim());
                }

                String contenidoLimpio = PATRON_IMAGEN.matcher(original).replaceAll("").trim();

                temasPorClave.put(clave, new GuiaTema(titulo, contenidoLimpio, List.copyOf(imagenes)));

                contexto.append("=== GUÍA: ").append(titulo).append(" ===\n")
                        .append(contenidoLimpio).append("\n")
                        .append("=== FIN GUÍA ===\n\n");
            }

            contextoGuias = contexto.toString().trim();
            log.info("Guías del Orientador cargadas: {} temas, {} caracteres de contexto",
                    temasPorClave.size(), contextoGuias.length());
        } catch (IOException e) {
            log.warn("No se pudieron cargar las guías del Orientador: {}", e.getMessage());
        }
    }

    @Override
    public String obtenerContextoGuias() {
        return contextoGuias;
    }

    @Override
    public String obtenerContextoPorTema(String clave) {
        GuiaTema tema = temasPorClave.get(clave);
        if (tema == null) return null;
        return "=== GUÍA: " + tema.titulo() + " ===\n"
                + tema.contenido() + "\n"
                + "=== FIN GUÍA ===";
    }

    @Override
    public List<String> obtenerImagenesPorTema(String clave) {
        GuiaTema tema = temasPorClave.get(clave);
        return tema != null ? tema.imagenes() : List.of();
    }

    @Override
    public List<String> obtenerVideosPorTema(String clave) {
        if (clave == null) return List.of();   // Map.ofEntries es inmutable y no admite key null
        return VIDEOS_POR_TEMA.getOrDefault(clave, List.of());
    }

    @Override
    public List<String> obtenerVideos(String pregunta, String tema) {
        if (pregunta != null && !pregunta.isBlank()) {
            String texto = pregunta.toLowerCase();
            LinkedHashSet<String> hits = new LinkedHashSet<>();
            for (Map.Entry<List<String>, List<String>> bucket : VIDEOS_POR_KEYWORD) {
                for (String palabra : bucket.getKey()) {
                    if (texto.contains(palabra)) { hits.addAll(bucket.getValue()); break; }
                }
            }
            if (!hits.isEmpty()) return List.copyOf(hits);
        }
        return obtenerVideosPorTema(tema);   // fallback: todos los videos del tema
    }

    private record GuiaTema(String titulo, String contenido, List<String> imagenes) {}
}
