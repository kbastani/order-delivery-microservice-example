package demo.order.action;

import demo.domain.Action;
import demo.driver.domain.*;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
import demo.driver.repository.DriverRepository;
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
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class AssignDriver extends Action<Order> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private DriverRepository driverRepository;
    private final DriverService driverService;
    private final OrderService orderService;

    public AssignDriver(DriverRepository driverRepository, DriverService driverService, OrderService orderService) {
        this.driverRepository = driverRepository;
        this.driverService = driverService;
        this.orderService = orderService;
    }

    @Transactional
    public Order apply(Order order, Long driverId) {
        checkOrderState(order);

        // Lookup the store and connect it to the order
        Driver driver = driverService.get(driverId);

        if (driver == null)
            throw new RuntimeException("The driver with the provided id does not exist.");

        checkDriverState(driver);

        order.setDriverId(driverId);
        order.setStatus(OrderStatus.DRIVER_ASSIGNED);

        driver.setActivityStatus(DriverActivityStatus.DRIVER_DELIVERING);
        driver.setOrderId(order.getIdentity());

        order = orderService.update(order);
        driver = driverService.update(driver);

        try {
            order.appendEvent(new OrderEvent(OrderEventType.DRIVER_ASSIGNED, order));
            driver.appendEvent(new DriverEvent(DriverEventType.ACCEPTED_ORDER_REQUEST));
        } catch (Exception ex) {
            log.error("Could not assign order to restaurant", ex);
            order.setStatus(OrderStatus.ORDER_PREPARED);
            order = orderService.update(order);
        }

        return order;
    }

    private void checkOrderState(Order order) {
        try {
            Assert.isTrue(order.getStatus() == OrderStatus.ORDER_PREPARED,
                    String.format("Order must be in a ORDER_PREPARED state. {state=%s}", order.getStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private void checkDriverState(Driver driver) {
        try {
            Assert.isTrue(driver.getDriverStatus() == DriverStatus.DRIVER_ACTIVE &&
                            driver.getAvailabilityStatus() == DriverAvailabilityStatus.DRIVER_ONLINE &&
                            driver.getActivityStatus() == DriverActivityStatus.DRIVER_WAITING,
                    String.format("Driver must be in a DRIVER_ACTIVE state. AvailabilityStatus must be in a " +
                                    "DRIVER_ONLINE state. ActivityStatus must be in a DRIVER_WAITING state. " +
                                    "{state=%s, availability=%s, activity=%s}",
                            driver.getDriverStatus(), driver.getAvailabilityStatus(), driver.getActivityStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

}
