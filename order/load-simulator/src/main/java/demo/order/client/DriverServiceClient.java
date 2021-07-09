package demo.order.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class DriverServiceClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final String baseUri;

    public DriverServiceClient(RestTemplate restTemplate, RetryTemplate retryTemplate,
                               @Value("${order-service.host:localhost}") String orderServiceHostName) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.baseUri = "http://" + orderServiceHostName + "/v1/drivers";
    }

    public Driver get(Long driverId) {
        Driver result;
        try {
            result = retryWith((id) -> restTemplate.getForObject(UriTemplate.of(baseUri + "/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(id), Driver.class), driverId);
        } catch (RestClientResponseException ex) {
            log.error("Get driver failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Driver create(Driver newDriver) {
        Driver result;
        try {
            result = retryWith((driver) -> restTemplate.postForObject(UriTemplate.of(baseUri).expand(),
                    driver, Driver.class), newDriver);
        } catch (RestClientResponseException ex) {
            log.error("Create driver failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }


    public Driver update(Driver updateDriver) {
        Driver result;
        try {
            result = retryWith((driver) -> restTemplate.exchange(new RequestEntity<>(driver, HttpMethod.PUT,
                    UriTemplate.of(baseUri + "/{id}").with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(driver.getDriverId())), Driver.class).getBody(), updateDriver);
        } catch (RestClientResponseException ex) {
            log.error("Update driver failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public boolean delete(Long driverId) {
        try {
            retryWith((id) -> {
                restTemplate.delete(UriTemplate.of(baseUri + "/{id}")
                        .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                        .expand(id));
                return true;
            }, driverId);
        } catch (RestClientResponseException ex) {
            log.error("Delete driver failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Driver activateAccount(Long driverId) {
        Driver result;
        try {
            result = retryWith((driverIdentity) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/activateAccount")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(driverIdentity), null, Driver.class), driverId);
        } catch (RestClientResponseException ex) {
            log.error("Could not activate driver account", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Driver driverOnline(Long driverId) {
        Driver result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/driverOnline")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Driver.class), driverId);
        } catch (RestClientResponseException ex) {
            log.error("Could not make driver go online", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Driver driverOffline(Long driverId) {
        Driver result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/driverOffline")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Driver.class), driverId);
        } catch (RestClientResponseException ex) {
            log.error("Could not make driver go offline", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Driver updateDriverLocation(Long driverId, Double lat, Double lon) {
        Driver result;
        try {
            URI uri = UriTemplate.of(baseUri +
                    "/{id}/commands/updateDriverLocation?lat=" + lat + "&lon=" + lon)
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(driverId);

            result = retryWith((v) -> restTemplate.postForObject(uri, null, Driver.class), null);
        } catch (RestClientResponseException ex) {
            log.error("Could not update driver location", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public DriverOrderRequest fetchOrderRequest(Long driverId) {
        DriverOrderRequest result;
        try {
            result = restTemplate.getForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/fetchOrderRequest")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(driverId), DriverOrderRequest.class);
        } catch (RestClientResponseException ex) {
            log.trace("Could not fetch nearby order request", ex);
            result = null;
        }

        return result;
    }

    private <T, R> R retryWith(Function<T, R> requestHandler, T param) {
        return retryTemplate.execute((RetryCallback<R, ResourceAccessException>) context -> {
            if (context.getRetryCount() > 0)
                log.info(context.toString());
            return requestHandler.apply(param);
        });
    }

    private <T, U, R> R biRetryWith(BiFunction<T, U, R> requestHandler, T p1, U p2) {
        return retryTemplate.execute((RetryCallback<R, ResourceAccessException>) context -> {
            if (context.getRetryCount() > 0)
                log.info(context.toString());
            return requestHandler.apply(p1, p2);
        });
    }

    private String getHttpStatusMessage(RestClientResponseException ex) {
        Map<String, String> errorMap = new HashMap<>();
        try {
            errorMap = new ObjectMapper().readValue(ex.getResponseBodyAsString(), errorMap.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorMap.getOrDefault("message", null);
    }
}
