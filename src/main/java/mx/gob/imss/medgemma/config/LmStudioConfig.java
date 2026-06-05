package mx.gob.imss.medgemma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;


@Data
@ConfigurationProperties(prefix = "lmstudio")
public class LmStudioConfig {
	private String url = "http://localhost:1234";
	private String apiToken = "";
	private String defaultModel = "medgemma-4b-it";
	private Integer contextLength = 4096;
	private Double temperature = 0.0;

	@Bean
	public WebClient lmStudioWebClient() {
		return WebClient.builder().baseUrl(url).defaultHeader("Content-Type", "application/json")
				.defaultHeader("Authorization", "Bearer " + apiToken)
				.codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
	}
}
