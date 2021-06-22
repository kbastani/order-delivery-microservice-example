package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.order.client.OrderServiceClient;
import demo.restaurant.config.RestaurantProperties;
import demo.restaurant.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class LoadSimulatorApplication {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public static void main(String[] args) {
        SpringApplication.run(LoadSimulatorApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(30000))
                .setReadTimeout(Duration.ofMillis(30000))
                .build();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(10)
                .exponentialBackoff(100, 10, 10000)
                .retryOn(RestClientException.class)
                .traversingCauses()
                .build();
    }

    @Bean
    @Profile("docker")
    public CommandLineRunner commandLineRunner(OrderServiceClient orderServiceClient) {
        return (args) -> {

            String file = resourceAsString(new ClassPathResource("/static/locations.json"));
            ObjectMapper mapper = new ObjectMapper();

            //log.info("Waiting 60 seconds before starting the order simulation...");
            //Thread.sleep(60000);

            List<Restaurant> restaurants =
                    Stream.of(mapper.readValue(file, Restaurant[].class))
                            .filter(restaurant -> restaurant.getCity().equals("San Francisco"))
                            .sorted(Comparator.comparingInt(Restaurant::getStoreId))
                            .limit(10)
                            .peek(restaurant ->
                                    restaurant.init(new RestaurantProperties(Math.round(Math.random() * 30000L) +
                                            30000L, 1000L, 15.0), orderServiceClient))
                            .collect(Collectors.toList());

            restaurants.forEach(restaurant -> {
                System.out.println(restaurant.toString());
                restaurant.open();
            });
        };
    }

    private static String resourceAsString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
