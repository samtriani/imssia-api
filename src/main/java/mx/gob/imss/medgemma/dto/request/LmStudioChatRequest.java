package mx.gob.imss.medgemma.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class LmStudioChatRequest {

    private String       model;
    private List<Message> messages;
    private Double       temperature;
    @JsonProperty("max_tokens")         private Integer      maxTokens;
    @JsonProperty("min_tokens")         private Integer      minTokens;
    @JsonProperty("top_p")              private Double       topP;
    @JsonProperty("repetition_penalty") private Double       repetitionPenalty;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> stop;

    @Data @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;
    }

    public static Message userMsg(String content)   { return new Message("user",   content); }
    public static Message systemMsg(String content) { return new Message("system", content); }
}
