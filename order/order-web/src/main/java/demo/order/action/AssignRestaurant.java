package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.restaurant.domain.Restaurant;
import demo.restaurant.domain.RestaurantRepository;
import demo.util.GeoUtils;
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
public class AssignRestaurant extends Action<Order> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RestaurantRepository restaurantRepository;
    private final OrderService orderService;

    public AssignRestaurant(RestaurantRepository restaurantRepository, OrderService orderService) {
        this.restaurantRepository = restaurantRepository;
        this.orderService = orderService;
    }

    public Order apply(Order order, Long restaurantId) {
        checkOrderState(order);

        // Lookup the store and connect it to the order
        Restaurant restaurant = restaurantRepository.findByStoreId(restaurantId).orElse(null);

        if (restaurant == null)
            throw new RuntimeException("The restaurant with the provided storeId does not exist.");

        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.ORDER_ASSIGNED);
        order.setDeliveryLat(restaurant.getLatitude());
        order.setDeliveryLon(restaurant.getLongitude());

        // TODO: After adding a Customer API with address resolution to geospatial coordinates, update this workflow
        generateFakeDeliveryLocation(order, restaurant);

        order = orderService.update(order);

        try {
            order.appendEvent(new OrderEvent(OrderEventType.ORDER_ASSIGNED, order));
        } catch (Exception ex) {
            log.error("Could not assign order to restaurant", ex);
            order.setStatus(OrderStatus.ORDER_CREATED);
            order = orderService.update(order);
        }

        return order;
    }

    private Order generateFakeDeliveryLocation(Order order, Restaurant restaurant) {
        // Generate a random delivery location 5km-10km from restaurant since customer API is not implemented
        double[] deliveryCoordinates = GeoUtils.findPointAtDistanceFrom(
                new double[]{restaurant.getLatitude(), restaurant.getLongitude()},
                (Math.PI * 2.0) * Math.random(),
                ((Math.random() / 2.0) + .5) * 10.0);

        order.setDeliveryLat(deliveryCoordinates[0]);
        order.setDeliveryLon(deliveryCoordinates[1]);
        return order;
    }

    private void checkOrderState(Order order) {
        try {
            Assert.isTrue(order.getStatus() == OrderStatus.ORDER_CREATED,
                    String.format("Order must be in a ORDER_CREATED state. {state=%s}", order.getStatus()));
        } catch (Exception ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

}
