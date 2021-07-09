package demo.order.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.order.domain.Order;
import demo.order.domain.Orders;
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
public class OrderServiceClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final String baseUri;

    public OrderServiceClient(RestTemplate restTemplate, RetryTemplate retryTemplate,
                              @Value("${order-service.host:localhost}") String orderServiceHostName) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.baseUri = "http://" + orderServiceHostName + "/v1/orders";
    }

    public Order get(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.getForObject(UriTemplate.of(baseUri + "/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(id), Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Get order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order create(Order newOrder) {
        Order result;
        try {
            result = retryWith((order) -> restTemplate.postForObject(UriTemplate.of(baseUri).expand(),
                    order, Order.class), newOrder);
        } catch (RestClientResponseException ex) {
            log.error("Create order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }


    public Order update(Order updateOrder) {
        Order result;
        try {
            result = retryWith((order) -> restTemplate.exchange(new RequestEntity<>(order, HttpMethod.PUT,
                    UriTemplate.of(baseUri + "/{id}").with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(order.getOrderId())), Order.class).getBody(), updateOrder);
        } catch (RestClientResponseException ex) {
            log.error("Update order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public boolean delete(Long orderId) {
        try {
            retryWith((id) -> {
                restTemplate.delete(UriTemplate.of(baseUri + "/{id}")
                        .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                        .expand(id));
                return true;
            }, orderId);
        } catch (RestClientResponseException ex) {
            log.error("Delete order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Orders findOrdersByAccountId(Long accountId) {
        Orders result;
        try {
            result = retryWith((id) -> restTemplate.getForObject
                    (UriTemplate.of(baseUri + "/search/findOrdersByAccountId")
                            .with("accountId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(id), Orders.class), accountId);
        } catch (RestClientResponseException ex) {
            log.error("Find orders by accountId failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order assignOrder(Long orderId, Integer restaurantId) {
        Order result;
        try {
            result = biRetryWith((orderIdentity, restaurantIdentity) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/assignOrder{?restaurantId}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .with("restaurantId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(orderIdentity, restaurantIdentity), null, Order.class), orderId, restaurantId);
        } catch (RestClientResponseException ex) {
            log.error("Assign order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order assignDriver(Long orderId, Long driverId) {
        Order result;
        try {
            result = restTemplate.postForObject(UriTemplate.of(baseUri + "/{id}/commands/assignDriver{?driverId}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .with("driverId", TemplateVariable.VariableType.REQUEST_PARAM)
                    .expand(orderId, driverId), null, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Assign driver to order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order prepareOrder(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/prepareOrder")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Assign order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order orderReady(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/orderReady")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Could not make order ready", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order updateOrderLocation(Long orderId, Double lat, Double lon) {
        Order result;
        try {
            URI uri = UriTemplate.of(baseUri +
                    "/{id}/commands/updateOrderLocation?lat=" + lat + "&lon=" + lon)
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(orderId);

            result = retryWith((v) -> restTemplate.postForObject(uri, null, Order.class), null);
        } catch (RestClientResponseException ex) {
            log.error("Could not update order location", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order orderPickedUp(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/orderPickedUp")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Could not pick up order", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order deliverOrder(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/deliverOrder")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(orderId), null, Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Could not start order delivery trip", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order orderDelivered(Long orderId) {
        Order result;
        try {
            result = retryWith((id) -> restTemplate.postForObject(
                    UriTemplate.of(baseUri + "/{id}/commands/orderDelivered")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(id), null, Order.class), orderId);
        } catch (RestClientResponseException ex) {
            log.error("Could not deliver order to customer", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
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
