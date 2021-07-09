package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverProperties;
import demo.order.client.*;
import demo.driver.domain.DriverActor;
import demo.restaurant.config.RestaurantProperties;
import demo.restaurant.domain.Restaurant;
import demo.restaurant.domain.RestaurantActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
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
@EnableEurekaClient
public class LoadSimulatorApplication {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public static void main(String[] args) {
        SpringApplication.run(LoadSimulatorApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(120000))
                .setReadTimeout(Duration.ofMillis(120000))
                .build();
    }

    @Bean
    @Profile({"docker", "development"})
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(10)
                .exponentialBackoff(1000, 10, 100000)
                .retryOn(RestClientException.class)
                .traversingCauses()
                .build();
    }

    @Bean
    @Profile({"docker", "development"})
    public CommandLineRunner commandLineRunner(OrderServiceClient orderServiceClient,
                                               DriverServiceClient driverServiceClient) {
        return (args) -> {

            String file = resourceAsString(new ClassPathResource("/locations.json"));
            ObjectMapper mapper = new ObjectMapper();

            // Create restaurants
            List<RestaurantActor> restaurants =
                    Stream.of(mapper.readValue(file, Restaurant[].class))
                            .filter(restaurant -> restaurant.getCity().equals("San Francisco"))
                            .sorted(Comparator.comparingInt(Restaurant::getStoreId))
                            .limit(50)
                            .map(restaurant -> {
                                RestaurantActor restaurantActor = new RestaurantActor();
                                restaurantActor.setRestaurant(restaurant);
                                restaurantActor.init(new RestaurantProperties(Math.round(Math.random() * 35000.0) +
                                        60000L, 1000L, 15.0), orderServiceClient);
                                return restaurantActor;
                            })
                            .collect(Collectors.toList());

            // Create drivers
            List<DriverActor> drivers = restaurants.stream()
                    .map(restaurantActor -> {
                        // Create one new driver per restaurant
                        Driver driver = driverServiceClient.create(new Driver());
                        driver = driverServiceClient.activateAccount(driver.getDriverId());
                        driver = driverServiceClient.driverOnline(driver.getDriverId());
                        driver = driverServiceClient.updateDriverLocation(driver.getDriverId(),
                                restaurantActor.getRestaurant().getLatitude(),
                                restaurantActor.getRestaurant().getLongitude());
                        DriverActor driverActor = new DriverActor(new DriverProperties(
                                5000L, 1000L, 15.0));
                        driverActor.setDriver(driver);
                        driverActor.init(driverServiceClient, orderServiceClient);
                        return driverActor;
                    }).collect(Collectors.toList());

            restaurants.forEach(restaurantActor -> {
                System.out.println(restaurantActor.toString());
                restaurantActor.open();
            });

            drivers.forEach(driverActor -> {
                System.out.println(driverActor.toString());
                driverActor.open();
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
