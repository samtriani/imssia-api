package mx.gob.imss.medgemma.service;
import mx.gob.imss.medgemma.dto.contexto.PacienteContextoDto;
public interface PacienteContextoService {
    PacienteContextoDto ensamblarContexto(String curp);
}
