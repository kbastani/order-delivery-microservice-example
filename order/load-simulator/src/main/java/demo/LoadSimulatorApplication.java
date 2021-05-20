package demo;

import demo.order.client.OrderClient;
import demo.restaurant.config.RestaurantProperties;
import demo.restaurant.domain.Restaurant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            List<Restaurant> restaurantList = IntStream.range(0, 1000)
                    .mapToObj(i -> Restaurant.from(new RestaurantProperties((long) i,
                            7500L,
                            1000L,
                            15.0), orderClient))
                    .collect(Collectors.toList());

            restaurantList.forEach(Restaurant::open);
        };
    }
}
