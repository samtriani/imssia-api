package mx.gob.imss.medgemma.service;

import mx.gob.imss.medgemma.dto.request.OrientadorChatRequest;
import mx.gob.imss.medgemma.dto.response.OrientadorChatResponse;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface OrientadorService {
    OrientadorChatResponse chat(OrientadorChatRequest request);

    /** Versión en streaming (SSE): evento "meta" (tema/imágenes/videos), luego "delta" por token, y "done". */
    Flux<ServerSentEvent<Object>> chatStream(OrientadorChatRequest request);
}
