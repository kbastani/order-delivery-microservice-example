package demo.order.domain;

import demo.domain.Service;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@org.springframework.stereotype.Service
public class OrderService extends Service<Order, Long> {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order registerOrder(Order order) {

        order = create(order);

        // Trigger the order creation event
        order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_CREATED, order));

        return order;
    }

    /**
     * Create a new {@link Order} entity.
     *
     * @param order is the {@link Order} to create
     * @return the newly created {@link Order}
     */
    public Order create(Order order) {

        // Save the order to the repository
        order = orderRepository.saveAndFlush(order);

        return order;
    }

    /**
     * Get an {@link Order} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Order} entity
     * @return an {@link Order} entity
     */
    public Order get(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Update an {@link Order} entity with the supplied identifier.
     *
     * @param order is the {@link Order} containing updated fields
     * @return the updated {@link Order} entity
     */
    @Transactional
    public Order update(Order order) {
        Assert.notNull(order.getIdentity(), "Order id must be present in the resource URL");
        Assert.notNull(order, "Order request body cannot be null");

        Assert.state(orderRepository.existsById(order.getIdentity()),
                "The order with the supplied id does not exist");

        Order currentOrder = get(order.getIdentity());
        currentOrder.setAccountId(order.getAccountId());
        currentOrder.setRestaurantId(order.getRestaurantId());
        currentOrder.setStatus(order.getStatus());

        return orderRepository.saveAndFlush(currentOrder);
    }

    /**
     * Delete the {@link Order} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Order}
     */
    public boolean delete(Long id) {
        Assert.state(orderRepository.existsById(id),
                "The order with the supplied id does not exist");
        this.orderRepository.deleteById(id);
        return true;
    }

    public Orders findOrdersByAccountId(Long accountId) {
        return new Orders(orderRepository.findOrdersByAccountId(accountId));
    }
}
