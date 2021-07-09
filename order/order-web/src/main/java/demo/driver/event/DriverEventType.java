package demo.driver.event;

import demo.driver.domain.Driver;
import demo.driver.domain.DriverAvailabilityStatus;

/**
 * The {@link DriverEventType} represents a collection of possible events that describe state transitions of
 * {@link DriverAvailabilityStatus} on the {@link Driver} aggregate.
 *
 * @author Kenny Bastani
 */
public enum DriverEventType {
    DRIVER_CREATED,
    DRIVER_WENT_ONLINE,
    DRIVER_WENT_OFFLINE,
    LOCATION_UPDATED,
    ACCOUNT_ACTIVATED,
    ORDER_DELIVERED,
    ACCOUNT_SUSPENDED,
    ACCEPTED_ORDER_REQUEST,
    DECLINED_ORDER_REQUEST,
    RECEIVED_ORDER_REQUEST,
    ORDER_PICKED_UP,
    ORDER_DELIVERING
}
