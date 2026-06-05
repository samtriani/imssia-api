package mx.gob.imss.medgemma.service.impl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.gob.imss.medgemma.dto.contexto.*;
import mx.gob.imss.medgemma.entity.*;
import mx.gob.imss.medgemma.repository.*;
import mx.gob.imss.medgemma.service.PacienteContextoService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class PacienteContextoServiceImpl implements PacienteContextoService {

    private final PamtPacienteRepository pacienteRepo;
    private final PamtExpedienteClinicoRepository expedienteRepo;
    private final PamtConsultaRepository consultaRepo;
    private final PamtConsultaDiagnosticoRepository dxRepo;
    private final PamtConsultaPrescripcionRepository rxRepo;
    private final PamtAntecedenteHeredofamiliarRepository heredoRepo;
    private final PamtAntecedentePatologicoRepository patologicoRepo;
    private final PamtAntecedenteAlergiaRepository alergiaRepo;
    private final PamtAntecedenteNoPatologicoRepository noPatologicoRepo;
    private final PamtVacunaAplicadaRepository vacunaRepo;

    @Override
    public PacienteContextoDto ensamblarContexto(String curp) {
        log.info("Ensamblando contexto clínico para CURP: {}", curp);

        PamtPaciente paciente = pacienteRepo.findByCveCurpAndIndActivoTrue(curp.toUpperCase())
            .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado: " + curp));

        PamtExpedienteClinico expediente = expedienteRepo
            .findByIdPaciente(paciente.getIdPaciente()).orElse(null);

        String edad = calcularEdad(paciente.getFecNacimiento());
        String nombreCompleto = (paciente.getNomNombre() + " " + paciente.getNomPrimerApellido()
            + (paciente.getNomSegundoApellido() != null ? " " + paciente.getNomSegundoApellido() : "")).trim();

        if (expediente == null) {
            log.warn("Sin expediente para CURP {}", curp);
            return PacienteContextoDto.builder()
                .curp(paciente.getCveCurp()).nss(paciente.getNumNss())
                .nombreCompleto(nombreCompleto).sexo(mapSexo(paciente.getDesSexo()))
                .edad(edad).fechaNacimiento(paciente.getFecNacimiento().toString())
                .tipoSangre(paciente.getTipoSangre() != null ? paciente.getTipoSangre().getDesTipoSangre() : null)
                .unidadAdscripcion(paciente.getUnidadAdscripcion() != null ? paciente.getUnidadAdscripcion().getDesUnidad() : null)
                .alergias(Collections.emptyList()).antecedentesPatologicos(Collections.emptyList())
                .antecedentesHeredofamiliares(Collections.emptyList())
                .ultimasConsultas(Collections.emptyList()).vacunasAplicadas(Collections.emptyList())
                .build();
        }

        UUID idExp = expediente.getIdExpediente();

        // Últimas 3 consultas
        List<ConsultaResumenDto> consultas = consultaRepo.findLastN(idExp, 3).stream()
            .map(c -> {
                List<String> dxList = dxRepo.findByIdConsulta(c.getIdConsulta()).stream()
                    .map(d -> d.getCie10().getCveCie10() + ": " + d.getCie10().getDesDiagnostico())
                    .collect(Collectors.toList());
                List<String> rxList = rxRepo.findByIdConsulta(c.getIdConsulta()).stream()
                    .map(r -> r.getMedicamento().getDesMedicamento() + " " + r.getDesDosis() + " " + r.getDesFrecuencia())
                    .collect(Collectors.toList());
                return ConsultaResumenDto.builder()
                    .fechaConsulta(c.getFecConsulta() != null
                        ? c.getFecConsulta().atZone(ZoneId.of("America/Mexico_City")).toLocalDate().toString() : "—")
                    .tipoConsulta(c.getDesTipoConsulta())
                    .signosVitales(buildSignosVitales(c))
                    .diagnosticos(dxList).medicamentos(rxList)
                    .motivoConsulta(c.getDesMotivoConsulta())
                    .analisis(c.getDesAnalisis()).plan(c.getDesPlan())
                    .build();
            }).collect(Collectors.toList());

        List<AntecedenteHeredofamiliarDto> heredos = heredoRepo.findByIdExpediente(idExp).stream()
            .map(h -> AntecedenteHeredofamiliarDto.builder()
                .parentesco(h.getDesParentesco())
                .cveCie10(h.getCie10() != null ? h.getCie10().getCveCie10() : null)
                .padecimiento(h.getDesPadecimiento()).build())
            .collect(Collectors.toList());

        List<AntecedentePatologicoDto> patologicos = patologicoRepo.findByIdExpediente(idExp).stream()
            .map(p -> AntecedentePatologicoDto.builder()
                .cveCie10(p.getCie10() != null ? p.getCie10().getCveCie10() : null)
                .padecimiento(p.getDesPadecimiento())
                .fechaInicio(p.getFecInicio() != null ? p.getFecInicio().toString() : null)
                .tratamientoActual(p.getDesTratamientoActual())
                .indCronico(p.getIndCronico()).indControlado(p.getIndControlado()).build())
            .collect(Collectors.toList());

        List<AlergiaDto> alergias = alergiaRepo.findByIdExpediente(idExp).stream()
            .map(a -> AlergiaDto.builder()
                .alergeno(a.getDesAlergeno()).tipo(a.getDesTipo())
                .reaccion(a.getDesReaccion()).severidad(a.getDesSeveridad()).build())
            .collect(Collectors.toList());

        AntecedenteNoPatologicoDto noPatologico = noPatologicoRepo.findByIdExpediente(idExp)
            .map(n -> AntecedenteNoPatologicoDto.builder()
                .tabaquismo(n.getDesTabaquismo()).numCigarrosDia(n.getNumCigarrosDia())
                .alcoholismo(n.getDesAlcoholismo()).actividadFisica(n.getDesActividadFisica())
                .alimentacion(n.getDesAlimentacion()).ocupacion(n.getDesOcupacion())
                .escolaridad(n.getDesEscolaridad()).build())
            .orElse(null);

        List<VacunaDto> vacunas = vacunaRepo.findByIdExpediente(idExp).stream()
            .map(v -> VacunaDto.builder()
                .vacuna(v.getDesVacuna()).numDosis(v.getNumDosis())
                .fechaAplicacion(v.getFecAplicacion().toString()).lote(v.getDesLote()).build())
            .collect(Collectors.toList());

        return PacienteContextoDto.builder()
            .curp(paciente.getCveCurp()).nss(paciente.getNumNss())
            .nombreCompleto(nombreCompleto).sexo(mapSexo(paciente.getDesSexo()))
            .edad(edad).fechaNacimiento(paciente.getFecNacimiento().toString())
            .tipoSangre(paciente.getTipoSangre() != null ? paciente.getTipoSangre().getDesTipoSangre() : null)
            .unidadAdscripcion(paciente.getUnidadAdscripcion() != null ? paciente.getUnidadAdscripcion().getDesUnidad() : null)
            .turno(paciente.getDesTurno())
            .antecedentesHeredofamiliares(heredos).antecedentesPatologicos(patologicos)
            .alergias(alergias).antecedentesNoPatologicos(noPatologico)
            .ultimasConsultas(consultas).vacunasAplicadas(vacunas)
            .build();
    }

    private String mapSexo(String s) { return "F".equals(s) ? "Femenino" : "M".equals(s) ? "Masculino" : "Indeterminado"; }

    private String calcularEdad(LocalDate fec) {
        if (fec == null) return "No registrada";
        return Period.between(fec, LocalDate.now()).getYears() + " años";
    }

    private String buildSignosVitales(PamtConsulta c) {
        StringBuilder sv = new StringBuilder();
        if (c.getNumTensionSistolica() != null)
            sv.append("TA ").append(c.getNumTensionSistolica()).append("/").append(c.getNumTensionDiastolica()).append(" mmHg ");
        if (c.getNumFrecuenciaCardiaca() != null) sv.append("FC ").append(c.getNumFrecuenciaCardiaca()).append(" lpm ");
        if (c.getNumTemperatura() != null) sv.append("T ").append(c.getNumTemperatura()).append("°C ");
        if (c.getNumPesoKg() != null) sv.append("Peso ").append(c.getNumPesoKg()).append("kg ");
        if (c.getNumImc() != null) sv.append("IMC ").append(c.getNumImc()).append(" ");
        if (c.getNumGlucosaCapilar() != null) sv.append("Gluc ").append(c.getNumGlucosaCapilar()).append("mg/dL");
        return sv.toString().trim();
    }
}
