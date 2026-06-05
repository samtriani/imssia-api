package mx.gob.imss.medgemma.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AnalizarNotasRequest {
    private String curp;
    private String numMatricula;
    private String especialidadMedico;
    private String notaTexto;
    private List<String> consultaIds;
    private String fechaIngreso;
    private String fechaEgreso;
}
