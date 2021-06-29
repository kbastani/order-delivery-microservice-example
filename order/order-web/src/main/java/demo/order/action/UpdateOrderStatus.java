package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates the status of a {@link Order} entity.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class UpdateOrderStatus extends Action<Order> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final OrderService orderService;

    public UpdateOrderStatus(OrderService orderService) {
        this.orderService = orderService;
    }

    public Order apply(Order order, OrderStatus orderStatus) {
        // Save rollback status
        OrderStatus rollbackStatus = order.getStatus();

        try {
            // Update status
            order.setStatus(orderStatus);
            order = orderService.update(order);
        } catch (Exception ex) {
            log.error("Could not update the status", ex);
            order.setStatus(rollbackStatus);
            order = orderService.update(order);
        }

        return order;
    }
}
