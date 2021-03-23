package demo.restaurant.domain;

import demo.order.domain.Order;

import java.util.UUID;

public class OrderDelivery {

    private final Long id;
    private Order order;

    public OrderDelivery() {
        id = (long)UUID.randomUUID().hashCode();
    }

    public OrderDelivery(Long id, Order order) {
        this.id = id;
        this.order = order;
    }

    public OrderDelivery(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderDelivery{" +
                "id=" + id +
                ", order=" + order +
                '}';
    }
}
