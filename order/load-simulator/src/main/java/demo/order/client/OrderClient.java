package demo.order.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.order.domain.Order;
import demo.order.domain.Orders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public OrderClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Order get(Long orderId) {
        Order result;
        try {
            result = restTemplate.getForObject(UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(orderId), Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Get order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order create(Order order) {
        Order result;
        try {
            result = restTemplate.postForObject(UriTemplate.of("http://order-delivery-service:8080/v1/orders").expand(),
                    order, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Create order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order update(Order order) {
        Order result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(order, HttpMethod.PUT,
                    UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(order.getOrderId())), Order.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public boolean delete(Long orderId) {
        try {
            restTemplate.delete(UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(orderId));
        } catch (RestClientResponseException ex) {
            log.error("Delete order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Orders findOrdersByAccountId(Long accountId) {
        Orders result;
        try {
            result = restTemplate
                    .getForObject(UriTemplate.of("http://order-delivery-service:8080/v1/orders/search/findOrdersByAccountId")
                            .with("accountId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(accountId), Orders.class);
        } catch (RestClientResponseException ex) {
            log.error("Delete order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order assignOrder(Long orderId, Long restaurantId) {
        Order result;
        try {
            result = restTemplate.postForObject(
                    UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}/commands/assignOrder{?restaurantId}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .with("restaurantId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(orderId, restaurantId), null, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Assign order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order prepareOrder(Long orderId) {
        Order result;
        try {
            result = restTemplate.postForObject(
                    UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}/commands/prepareOrder")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(orderId), null, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Assign order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    public Order orderReady(Long orderId) {
        Order result;
        try {
            result = restTemplate.postForObject(
                    UriTemplate.of("http://order-delivery-service:8080/v1/orders/{id}/commands/orderReady")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(orderId), null, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Assign order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    private String getHttpStatusMessage(RestClientResponseException ex) {
        Map<String, String> errorMap = new HashMap<>();
        try {
            errorMap = new ObjectMapper().readValue(ex.getResponseBodyAsString(), errorMap
                    .getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorMap.getOrDefault("message", null);
    }
}
