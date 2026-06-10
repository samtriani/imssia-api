package mx.gob.imss.medgemma.service;

import mx.gob.imss.medgemma.dto.request.OrientadorChatRequest;
import mx.gob.imss.medgemma.dto.response.OrientadorChatResponse;

public interface OrientadorService {
    OrientadorChatResponse chat(OrientadorChatRequest request);
}
