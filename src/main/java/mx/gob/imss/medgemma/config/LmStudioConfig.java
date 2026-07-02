package mx.gob.imss.medgemma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "lmstudio")
public class LmStudioConfig {

    private String       url               = "http://localhost:1234";
    private String       apiToken          = "";
    private String       defaultModel      = "medgemma-4b-it";
    private Integer      maxTokens         = 1024;
    private Integer      minTokens         = null;
    private Double       temperature       = 0.1;
    private Double       topP              = 0.9;
    private Double       repetitionPenalty = null;
    private List<String> stop              = new ArrayList<>();

    @Bean
    public WebClient lmStudioWebClient() {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
