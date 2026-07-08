package mx.gob.imss.medgemma.service;

import java.util.List;

public interface GuiaSistemaService {

    /** Contexto concatenado de todas las guías rápidas de ECSUS, sin referencias a imágenes. */
    String obtenerContextoGuias();

    /**
     * Contexto de UNA sola guía (la del tema indicado), en el mismo formato
     * delimitado que {@link #obtenerContextoGuias()}. Devuelve {@code null}
     * si el tema no existe. Usado para inyectar solo la guía relevante (RAG).
     */
    String obtenerContextoPorTema(String clave);

    /** Rutas (relativas a /assets) de las capturas de pantalla del tema indicado. */
    List<String> obtenerImagenesPorTema(String clave);

    /** URLs de los videos del tema indicado (vista general del tema). */
    List<String> obtenerVideosPorTema(String clave);

    /**
     * Selección fina de videos para la pregunta concreta: primero intenta empatar
     * por palabra clave (p. ej. "receta" → solo videos de receta); si no hay
     * coincidencia específica, cae al conjunto de videos del tema.
     */
    List<String> obtenerVideos(String pregunta, String tema);
}
