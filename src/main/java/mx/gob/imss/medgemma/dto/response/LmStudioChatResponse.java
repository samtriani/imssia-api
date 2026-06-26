package mx.gob.imss.medgemma.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class LmStudioChatResponse {

    private String id;
    private String model;
    private List<ChoiceDto> choices;
    private UsageDto usage;
    private TimingsDto timings;

    /** Texto de la respuesta — mismo contrato que antes. */
    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            MessageDto msg = choices.get(0).getMessage();
            return msg != null ? msg.getContent() : null;
        }
        return null;
    }

    /** Alias de {@code id} — compatibilidad con callers existentes. */
    public String getResponseId() { return id; }

    /** Alias de {@code model} — compatibilidad con callers existentes. */
    public String getModelInstanceId() { return model; }

    /**
     * Bridge de compatibilidad: envuelve usage + timings en el mismo
     * contrato que tenía StatsDto (totalOutputTokens, tokensPerSecond).
     */
    public StatsCompat getStats() {
        if (usage == null && timings == null) return null;
        return new StatsCompat(
            usage   != null ? usage.getCompletionTokens()      : null,
            timings != null ? timings.getPredictedPerSecond()  : null
        );
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChoiceDto {
        private MessageDto message;
        @JsonProperty("finish_reason") private String finishReason;
        private Integer index;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageDto {
        private String role;
        private String content;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageDto {
        @JsonProperty("completion_tokens") private Integer completionTokens;
        @JsonProperty("prompt_tokens")     private Integer promptTokens;
        @JsonProperty("total_tokens")      private Integer totalTokens;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimingsDto {
        @JsonProperty("predicted_per_second") private Double predictedPerSecond;
    }

    /** Reemplaza al antiguo StatsDto — mismo contrato para los callers. */
    @Data @AllArgsConstructor
    public static class StatsCompat {
        private Integer totalOutputTokens;
        private Double  tokensPerSecond;
    }
}
