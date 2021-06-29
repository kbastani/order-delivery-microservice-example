package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderService;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class UpdateOrderLocation extends Action<Order> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final OrderService orderService;

    public UpdateOrderLocation(OrderService orderService) {
        this.orderService = orderService;
    }

    public Order apply(Order order, Double lat, Double lon) {
        order.setLat(lat);
        order.setLon(lon);
        order = orderService.update(order);

        try {
            order.appendEvent(new OrderEvent(OrderEventType.ORDER_LOCATION_UPDATED, order));
        } catch (Exception ex) {
            log.error("Could not update order location", ex);
        }

        return order;
    }
}
