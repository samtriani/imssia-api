package mx.gob.imss.medgemma.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detecta a qué tema de la guía rápida de ECSUS pertenece la pregunta del
 * usuario, usando coincidencia de palabras clave (mismo enfoque que
 * {@link SystemPromptBuilder#detectarEspecialidad(String, int)}).
 *
 * Las claves devueltas corresponden a los nombres (sin extensión) de los
 * archivos en {@code src/main/resources/guias/}.
 */
public class GuiaTopicDetector {

    public static final String INICIO_SESION      = "01-inicio-sesion";
    public static final String BUSQUEDA_PACIENTES = "02-busqueda-pacientes";
    public static final String HISTORIA_CLINICA   = "03-historia-clinica";
    public static final String AGENDA_CITAS       = "04-agenda-citas";
    public static final String NOTA_MEDICA        = "05-nota-medica";
    public static final String AUXILIARES_DX_TX   = "06-auxiliares-dx-tx";

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();
    static {
        KEYWORDS.put(INICIO_SESION, List.of(
                "iniciar sesion", "iniciar sesión", "inicio de sesion", "inicio de sesión",
                "inicio sesion", "inicio sesión", "iniciar la sesion", "iniciar la sesión",
                "login", "acceso", "acceso al sistema", "ingresar al sistema", "entrar al sistema",
                "ingreso al sistema", "contraseña", "contrasena",
                "matricula", "matrícula", "olvide mi contraseña", "olvidé mi contraseña",
                "restablecer contraseña", "restablecer contrasena",
                "no puedo entrar", "credenciales"));

        KEYWORDS.put(BUSQUEDA_PACIENTES, List.of(
                "buscar paciente", "búsqueda de paciente", "busqueda de paciente",
                "curp", "nss", "datos de contacto", "registrar paciente",
                "encontrar paciente", "localizar paciente"));

        KEYWORDS.put(HISTORIA_CLINICA, List.of(
                "historia clinica", "historia clínica", "antecedentes", "heredofamiliares",
                "heredo familiares", "alergias", "padecimiento actual",
                "antecedentes quirurgicos", "antecedentes quirúrgicos",
                "antecedentes traumaticos", "antecedentes traumáticos",
                "antecedentes transfusionales", "enfermedades de la infancia"));

        KEYWORDS.put(AGENDA_CITAS, List.of(
                "cita", "citas", "agenda", "agendar", "cancelar cita", "modificar cita",
                "confirmar cita", "consulta programada", "ofertas de citas", "horario"));

        KEYWORDS.put(NOTA_MEDICA, List.of(
                "nota medica", "nota médica", "resumen clinico", "resumen clínico",
                "diagnostico", "diagnóstico", "addendum", "consulta de notas",
                "guardar nota", "exploracion fisica", "exploración física"));

        KEYWORDS.put(AUXILIARES_DX_TX, List.of(
                "receta", "medicamento", "laboratorio", "rayos x", "rayos equis",
                "estudio", "estudios", "solicitud de laboratorio", "solicitud de rayos",
                "auxiliares de diagnostico", "auxiliares de diagnóstico", "tratamiento",
                "imprimir receta"));
    }

    /**
     * @param pregunta texto de la pregunta del usuario
     * @return clave del tema detectado, o {@code null} si ninguna palabra clave coincide
     */
    public static String detectarTema(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) return null;
        String texto = pregunta.toLowerCase();

        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            for (String palabra : entry.getValue()) {
                if (texto.contains(palabra)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
