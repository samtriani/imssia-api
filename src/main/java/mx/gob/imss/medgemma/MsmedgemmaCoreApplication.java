package mx.gob.imss.medgemma;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import mx.gob.imss.medgemma.config.LmStudioConfig;

@SpringBootApplication
@EnableConfigurationProperties(LmStudioConfig.class)
public class MsmedgemmaCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsmedgemmaCoreApplication.class, args);
    }
}
