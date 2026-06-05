package mx.gob.imss.medgemma.service;
import mx.gob.imss.medgemma.dto.response.ImssAiChatResponse;
import mx.gob.imss.medgemma.dto.response.LmStudioChatResponse;
public interface LmStudioService {
    LmStudioChatResponse chatSimple(String input);
    LmStudioChatResponse chatSimple(String input, String model);
}
