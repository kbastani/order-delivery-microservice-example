package demo.order.event;

import demo.order.domain.Order;
import demo.order.domain.OrderStatus;

/**
 * The {@link OrderEventType} represents a collection of possible events that describe state transitions of
 * {@link OrderStatus} on the {@link Order} aggregate.
 *
 * @author Kenny Bastani
 */
public enum OrderEventType {
    ORDER_CREATED,
    ORDER_ASSIGNED,
    ORDER_PREPARING,
    ORDER_PREPARED,
    DRIVER_ASSIGNED,
    ORDER_PICKED_UP,
    ORDER_DELIVERING,
    ORDER_DELIVERED,
    ORDER_LOCATION_UPDATED
}
