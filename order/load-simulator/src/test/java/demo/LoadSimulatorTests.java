package demo;

import demo.order.client.OrderServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

@SpringBootTest
@ActiveProfiles("test")
public class LoadSimulatorTests {

	@Mock
	private OrderServiceClient orderServiceClient;

	@Test
	public void contextLoads() {
	}

	@Configuration
	public static class RetryConfig {
		@Bean
		@Order(1)
		public RetryTemplate retryTemplate() {
			return RetryTemplate.builder()
					.maxAttempts(2)
					.exponentialBackoff(100, 10, 1000)
					.retryOn(RestClientException.class)
					.traversingCauses()
					.build();
		}
	}
}
