package mx.gob.imss.medgemma.dto.response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
@Data @JsonIgnoreProperties(ignoreUnknown=true)
public class LmStudioChatResponse {
    @JsonProperty("model_instance_id") private String modelInstanceId;
    @JsonProperty("response_id") private String responseId;
    private List<OutputDto> output;
    private StatsDto stats;

    public String getContent() {
        if (output != null && !output.isEmpty()) return output.get(0).getContent();
        return null;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown=true)
    public static class OutputDto {
        private String type;
        private String content;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown=true)
    public static class StatsDto {
        @JsonProperty("input_tokens") private Integer inputTokens;
        @JsonProperty("total_output_tokens") private Integer totalOutputTokens;
        @JsonProperty("tokens_per_second") private Double tokensPerSecond;
        @JsonProperty("time_to_first_token_seconds") private Double timeToFirstTokenSeconds;
    }
}
