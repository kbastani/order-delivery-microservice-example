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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class OrderPickedUp extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Order apply(Order order) {
        Assert.isTrue(order
                .getStatus() == OrderStatus.ORDER_PREPARED, "Order must be in an ORDER_PREPARED state");

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        order.setStatus(OrderStatus.ORDER_PICKED_UP);
        order = orderService.update(order);

        try {
            // Trigger the payment connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_PICKED_UP, order));
        } catch (Exception ex) {
            log.error("Could not pick up prepared order from restaurant", ex);
            order.setStatus(OrderStatus.ORDER_PREPARED);
            order = orderService.update(order);
        }

        return order;
    }
}
