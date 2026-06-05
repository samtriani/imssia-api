package mx.gob.imss.medgemma.dto.contexto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class PacienteContextoDto {
    private String curp;
    private String nss;
    private String nombreCompleto;
    private String sexo;
    private String edad;
    private String fechaNacimiento;
    private String tipoSangre;
    private String unidadAdscripcion;
    private String turno;
    private List<AntecedenteHeredofamiliarDto> antecedentesHeredofamiliares;
    private List<AntecedentePatologicoDto>     antecedentesPatologicos;
    private List<AlergiaDto>                   alergias;
    private AntecedenteNoPatologicoDto         antecedentesNoPatologicos;
    private List<ConsultaResumenDto>           ultimasConsultas;
    private List<VacunaDto>                    vacunasAplicadas;

    public String toPromptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONTEXTO CLÍNICO DEL PACIENTE ===\n\n");
        sb.append("DATOS GENERALES:\n");
        sb.append("- Nombre: ").append(nombreCompleto).append("\n");
        sb.append("- Sexo: ").append(sexo).append("\n");
        sb.append("- Edad: ").append(edad).append("\n");
        sb.append("- Fecha de nacimiento: ").append(fechaNacimiento).append("\n");
        sb.append("- CURP: ").append(curp).append("\n");
        sb.append("- NSS: ").append(nss != null ? nss : "No registrado").append("\n");
        sb.append("- Tipo de sangre: ").append(tipoSangre != null ? tipoSangre : "No registrado").append("\n");
        sb.append("- Unidad: ").append(unidadAdscripcion != null ? unidadAdscripcion : "No registrada").append("\n");

        if (alergias != null && !alergias.isEmpty()) {
            sb.append("\n⚠️ ALERGIAS:\n");
            alergias.forEach(a -> sb.append("- ").append(a.getAlergeno())
                .append(" (").append(a.getTipo()).append(") — Severidad: ").append(a.getSeveridad())
                .append(" — ").append(a.getReaccion()).append("\n"));
        } else {
            sb.append("\n- Sin alergias registradas.\n");
        }

        if (antecedentesPatologicos != null && !antecedentesPatologicos.isEmpty()) {
            sb.append("\nANTECEDENTES PATOLÓGICOS:\n");
            antecedentesPatologicos.forEach(a -> {
                sb.append("- ").append(a.getPadecimiento());
                if (a.getCveCie10() != null) sb.append(" [").append(a.getCveCie10()).append("]");
                if (a.getFechaInicio() != null) sb.append(" desde ").append(a.getFechaInicio());
                sb.append(Boolean.TRUE.equals(a.getIndCronico()) ? " (crónico" : " (agudo");
                if (a.getIndControlado() != null) sb.append(a.getIndControlado() ? ", controlado)" : ", no controlado)");
                else sb.append(")");
                if (a.getTratamientoActual() != null) sb.append(" — Tto: ").append(a.getTratamientoActual());
                sb.append("\n");
            });
        }

        if (antecedentesHeredofamiliares != null && !antecedentesHeredofamiliares.isEmpty()) {
            sb.append("\nANTECEDENTES HEREDOFAMILIARES:\n");
            antecedentesHeredofamiliares.forEach(a -> sb.append("- ").append(a.getParentesco())
                .append(": ").append(a.getPadecimiento()).append("\n"));
        }

        if (antecedentesNoPatologicos != null) {
            AntecedenteNoPatologicoDto np = antecedentesNoPatologicos;
            sb.append("\nHÁBITOS:\n");
            sb.append("- Tabaquismo: ").append(np.getTabaquismo() != null ? np.getTabaquismo() : "NR");
            if ("ACTIVO".equals(np.getTabaquismo()) && np.getNumCigarrosDia() != null)
                sb.append(" (").append(np.getNumCigarrosDia()).append(" cig/día)");
            sb.append("\n- Alcoholismo: ").append(np.getAlcoholismo() != null ? np.getAlcoholismo() : "NR");
            sb.append("\n- Actividad física: ").append(np.getActividadFisica() != null ? np.getActividadFisica() : "NR");
            if (np.getOcupacion() != null) sb.append("\n- Ocupación: ").append(np.getOcupacion());
            sb.append("\n");
        }

        if (ultimasConsultas != null && !ultimasConsultas.isEmpty()) {
            sb.append("\nÚLTIMAS CONSULTAS (máx. 3):\n");
            ultimasConsultas.forEach(c -> {
                sb.append("\n  [").append(c.getFechaConsulta()).append("] ").append(c.getTipoConsulta()).append("\n");
                if (c.getSignosVitales() != null) sb.append("  Signos: ").append(c.getSignosVitales()).append("\n");
                if (c.getDiagnosticos() != null && !c.getDiagnosticos().isEmpty())
                    sb.append("  Dx: ").append(String.join(", ", c.getDiagnosticos())).append("\n");
                if (c.getMedicamentos() != null && !c.getMedicamentos().isEmpty())
                    sb.append("  Rx: ").append(String.join(", ", c.getMedicamentos())).append("\n");
                if (c.getPlan() != null) sb.append("  Plan: ").append(c.getPlan()).append("\n");
            });
        }

        if (vacunasAplicadas != null && !vacunasAplicadas.isEmpty()) {
            sb.append("\nVACUNAS:\n");
            vacunasAplicadas.forEach(v -> sb.append("- ").append(v.getVacuna())
                .append(" dosis ").append(v.getNumDosis())
                .append(" — ").append(v.getFechaAplicacion()).append("\n"));
        }

        sb.append("\n=== FIN DEL CONTEXTO CLÍNICO ===\n");
        return sb.toString();
    }
}
