package mx.gob.imss.medgemma.service;
import mx.gob.imss.medgemma.dto.request.ImssAiChatRequest;
import mx.gob.imss.medgemma.dto.response.ImssAiChatResponse;
public interface ImssAiContextService {
    ImssAiChatResponse chatConContexto(ImssAiChatRequest request);
}
