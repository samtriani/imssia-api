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
    private static final Pattern PATRON_VIDEO  = Pattern.compile("\\[▶[^\\]]*\\]\\(([^)]+)\\)");
    private static final Pattern PATRON_TITULO = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);

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

                LinkedHashSet<String> videos = new LinkedHashSet<>();
                Matcher videoMatcher = PATRON_VIDEO.matcher(original);
                while (videoMatcher.find()) {
                    videos.add(videoMatcher.group(1).trim());
                }

                String contenidoLimpio = PATRON_VIDEO.matcher(
                        PATRON_IMAGEN.matcher(original).replaceAll("")).replaceAll("").trim();

                temasPorClave.put(clave, new GuiaTema(titulo, contenidoLimpio, List.copyOf(imagenes), List.copyOf(videos)));

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
    public List<String> obtenerImagenesPorTema(String clave) {
        GuiaTema tema = temasPorClave.get(clave);
        return tema != null ? tema.imagenes() : List.of();
    }

    @Override
    public List<String> obtenerVideosPorTema(String clave) {
        GuiaTema tema = temasPorClave.get(clave);
        return tema != null ? tema.videos() : List.of();
    }

    private record GuiaTema(String titulo, String contenido, List<String> imagenes, List<String> videos) {}
}
