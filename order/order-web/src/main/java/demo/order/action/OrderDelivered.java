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

/**
 * Reserves inventory for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class OrderDelivered extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(OrderDelivered.class);

    public OrderDelivered() {
    }

    public Order apply(Order order) {
        try {
        Assert.isTrue(order
                .getStatus() == OrderStatus.ORDER_DELIVERING, "Order must be in an ORDER_DELIVERING state");
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        order.setStatus(OrderStatus.ORDER_DELIVERED);
        order = orderService.update(order);

        try {
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_DELIVERED, order));
        } catch (Exception ex) {
            log.error("Could not complete delivery", ex);
            order.setStatus(OrderStatus.ORDER_DELIVERING);
            order = orderService.update(order);
        }

        return order;
    }
}
