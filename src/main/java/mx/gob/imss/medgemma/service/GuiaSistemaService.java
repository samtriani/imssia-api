package mx.gob.imss.medgemma.service;

import java.util.List;

public interface GuiaSistemaService {

    /** Contexto concatenado de todas las guías rápidas de ECSUS, sin referencias a imágenes. */
    String obtenerContextoGuias();

    /** Rutas (relativas a /assets) de las capturas de pantalla del tema indicado. */
    List<String> obtenerImagenesPorTema(String clave);
}
