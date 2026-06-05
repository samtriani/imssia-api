package mx.gob.imss.medgemma.service;

import mx.gob.imss.medgemma.dto.request.LoginRequest;
import mx.gob.imss.medgemma.dto.response.LoginResponse;

public interface AuthService {

    /**
     * Valida las credenciales del médico contra pamt_usuario_medico.
     * Usa bcrypt via pgcrypto para comparar el password.
     *
     * @param request matricula + password
     * @return sesión con especialidad del médico
     * @throws jakarta.persistence.EntityNotFoundException si matrícula no existe
     * @throws org.springframework.security.authentication.BadCredentialsException si password incorrecto
     */
    LoginResponse login(LoginRequest request);
}
