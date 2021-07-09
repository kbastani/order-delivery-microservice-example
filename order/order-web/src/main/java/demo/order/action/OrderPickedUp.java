package demo.order.action;

import demo.domain.Action;
import demo.driver.domain.Driver;
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

@Service
@Transactional
public class OrderPickedUp extends Action<Order> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final OrderService orderService;
    private final DriverService driverService;

    public OrderPickedUp(OrderService orderService, DriverService driverService) {
        this.orderService = orderService;
        this.driverService = driverService;
    }

    public Order apply(Order order) {
        checkOrderState(order);

        Driver driver = driverService.get(order.getDriverId());
        order.setStatus(OrderStatus.ORDER_PICKED_UP);
        order = orderService.update(order);

        try {
            order.appendEvent(new OrderEvent(OrderEventType.ORDER_PICKED_UP, order));
            driver.appendEvent(new DriverEvent(DriverEventType.ORDER_PICKED_UP, driver));
        } catch (Exception ex) {
            log.error("Could not pick up prepared order from restaurant", ex);
            order.setStatus(OrderStatus.DRIVER_ASSIGNED);
            order = orderService.update(order);
        }

        return order;
    }

    private void checkOrderState(Order order) {
        try {
            Assert.isTrue(order.getStatus() == OrderStatus.DRIVER_ASSIGNED,
                    String.format("Order must be in a DRIVER_ASSIGNED state. {state=%s}", order.getStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
