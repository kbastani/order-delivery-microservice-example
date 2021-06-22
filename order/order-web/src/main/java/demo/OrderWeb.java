package demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.restaurant.domain.Restaurant;
import demo.restaurant.domain.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Example;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class OrderWeb {

    public static void main(String[] args) {
        SpringApplication.run(OrderWeb.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(RestaurantRepository restaurantRepository) {
        // Initializes the restaurant database on startup
        return (args) -> {
            String file = resourceAsString(new ClassPathResource("/locations.json"));
            ObjectMapper mapper = new ObjectMapper();

            try {
                restaurantRepository.saveAll(Stream.of(mapper.readValue(file, Restaurant[].class))
                        .filter(restaurant -> restaurant.getCity().equals("San Francisco"))
                        .sorted(Comparator.comparingLong(Restaurant::getStoreId))
                        .limit(10)
                        .filter(restaurant -> !restaurantRepository.exists(Example.of(restaurant)))
                        .collect(Collectors.toList()));

                System.out.println(Arrays.toString(restaurantRepository.findAll().toArray(new Restaurant[]{})));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
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
