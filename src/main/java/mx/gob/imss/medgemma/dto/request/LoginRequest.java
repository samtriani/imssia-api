package mx.gob.imss.medgemma.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String matricula;
    private String password;
}
