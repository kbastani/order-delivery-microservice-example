package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
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
public class DeliverOrder extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final OrderService orderService;

    public DeliverOrder(OrderService orderService) {
        this.orderService = orderService;
    }

    public Order apply(Order order) {
        checkOrderState(order);
        order.setStatus(OrderStatus.ORDER_DELIVERING);
        order = orderService.update(order);

        try {
            // Trigger the payment connected event
            order.appendEvent(new OrderEvent(OrderEventType.ORDER_DELIVERING, order));
        } catch (Exception ex) {
            log.error("Could not pick up prepared order from restaurant", ex);
            order.setStatus(OrderStatus.ORDER_PICKED_UP);
            order = orderService.update(order);
        }

        return order;
    }

    private void checkOrderState(Order order) {
        try {
            Assert.isTrue(order.getStatus() == OrderStatus.ORDER_PICKED_UP,
                    String.format("Order must be in a ORDER_PICKED_UP state. {state=%s}", order.getStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
