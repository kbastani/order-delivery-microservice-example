package demo.order.action;

import demo.domain.Action;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverActivityStatus;
import demo.driver.domain.DriverService;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
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

/**
 * Reserves inventory for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class OrderDelivered extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(OrderDelivered.class);
    private final OrderService orderService;
    private final DriverService driverService;

    public OrderDelivered(OrderService orderService, DriverService driverService) {
        this.orderService = orderService;
        this.driverService = driverService;
    }

    public Order apply(Order order) {
        checkOrderState(order);
        order.setStatus(OrderStatus.ORDER_DELIVERED);
        Driver driver = driverService.get(order.getDriverId());
        order = orderService.update(order);
        driver.setActivityStatus(DriverActivityStatus.DRIVER_WAITING);

        try {
            order.appendEvent(new OrderEvent(OrderEventType.ORDER_DELIVERED, order));
            driver.appendEvent(new DriverEvent(DriverEventType.ORDER_DELIVERED, driver));
        } catch (Exception ex) {
            log.error("Could not complete delivery", ex);
            order.setStatus(OrderStatus.ORDER_DELIVERING);
            order = orderService.update(order);
        }

        return order;
    }

    private void checkOrderState(Order order) {
        try {
            Assert.isTrue(order.getStatus() == OrderStatus.ORDER_DELIVERING,
                    String.format("Order must be in a ORDER_DELIVERING state. {state=%s}", order.getStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
