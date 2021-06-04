package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
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

    public Order apply(Order order, Double lat, Double lon) {
        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        Double oldLon = order.getLon();
        Double oldLat = order.getLat();

        order.setLat(lat);
        order.setLon(lon);

        order = orderService.update(order);

        try {
            // Trigger the account connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_LOCATION_UPDATED, order));
        } catch (Exception ex) {
            log.error("Could not update order location", ex);
            order.setLon(oldLon);
            order.setLat(oldLat);
            order = orderService.update(order);
        }

        return order;
    }
}
