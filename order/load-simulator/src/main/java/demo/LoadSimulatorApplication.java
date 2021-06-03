package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.order.client.OrderClient;
import demo.restaurant.config.RestaurantProperties;
import demo.restaurant.domain.Restaurant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class LoadSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadSimulatorApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();
    }

    @Bean
    public CommandLineRunner commandLineRunner(OrderClient orderClient) {
        return (args) -> {

            String file = resourceAsString(new ClassPathResource("/static/locations.json"));
            ObjectMapper mapper = new ObjectMapper();

            List<Restaurant> restaurants =
                    Stream.of(mapper.readValue(file, Restaurant[].class))
                            .peek(restaurant ->
                                    restaurant.init(new RestaurantProperties(7500L, 1000L, 15.0), orderClient))
                            .filter(restaurant -> restaurant.getCity().equals("San Francisco"))
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
