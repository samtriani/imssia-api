package mx.gob.imss.medgemma.service;

import mx.gob.imss.medgemma.dto.request.AnalizarNotasRequest;
import mx.gob.imss.medgemma.dto.response.AnalizarNotasResponse;
import mx.gob.imss.medgemma.dto.response.ConsultaBriefDto;
import java.util.List;

public interface CostosAnalisisService {
    AnalizarNotasResponse analizarNotas(AnalizarNotasRequest request);
    List<ConsultaBriefDto> obtenerConsultasPaciente(String curp);
}
