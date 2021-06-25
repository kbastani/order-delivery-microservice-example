package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Transactional
public class OrderReady extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public OrderReady() {
    }

    public Order apply(Order order) {
        try {
        Assert.isTrue(order
                .getStatus() == OrderStatus.ORDER_PREPARING, "Order must be in an ORDER_PREPARING state");
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        order.setStatus(OrderStatus.ORDER_PREPARED);
        order = orderService.update(order);

        try {
            // Trigger the payment connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_PREPARED, order));
        } catch (Exception ex) {
            log.error("Could not complete order preparation by restaurant", ex);
            order.setStatus(OrderStatus.ORDER_PREPARING);
            order = orderService.update(order);
        }

        return order;
    }
}
