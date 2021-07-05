package demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PinotConfig {

    @Bean
    public PinotJdbcTemplate pinotJdbcTemplate(@Value("${pinot.driver.uri:jdbc:pinot://localhost:9000}") String pinotUri) {
        return new PinotJdbcTemplate(pinotUri);
    }
}
