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

    // Videos por tema — administrados aquí, nunca en los .md, para no contaminar el contexto del LLM
    private static final Map<String, List<String>> VIDEOS_POR_TEMA = Map.of(
        "06-auxiliares-dx-tx", List.of(
            "http://msbovedaimss-documentos.apps.qaocp.imss.gob.mx/api/files/dcdc2d3d-959b-4010-b93a-1547da7d176e/145e2428-b206-4e6d-814c-b68dc889e728"
        )
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
    public List<String> obtenerImagenesPorTema(String clave) {
        GuiaTema tema = temasPorClave.get(clave);
        return tema != null ? tema.imagenes() : List.of();
    }

    @Override
    public List<String> obtenerVideosPorTema(String clave) {
        return VIDEOS_POR_TEMA.getOrDefault(clave, List.of());
    }

    private record GuiaTema(String titulo, String contenido, List<String> imagenes) {}
}
