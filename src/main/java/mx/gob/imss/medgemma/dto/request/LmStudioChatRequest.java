package mx.gob.imss.medgemma.dto.request;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class LmStudioChatRequest {
    private String model;
    private String input;
    private Double temperature;
    @JsonProperty("context_length") private Integer contextLength;
    @JsonInclude(JsonInclude.Include.NON_EMPTY) private List<IntegrationDto> integrations;

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IntegrationDto {
        private String type;
        private String id;
        @JsonProperty("server_label") private String serverLabel;
        @JsonProperty("server_url") private String serverUrl;
        @JsonProperty("allowed_tools") private List<String> allowedTools;
    }
}
