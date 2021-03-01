package demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enable JPA auditing on an empty configuration class to disable auditing on
 *
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
