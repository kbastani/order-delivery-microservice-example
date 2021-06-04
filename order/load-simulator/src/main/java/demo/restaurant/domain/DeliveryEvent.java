package demo.restaurant.domain;

import demo.order.domain.Order;

import java.util.UUID;
import java.util.function.Function;

public class DeliveryEvent {

    private final Long id;
    private Order order;
    private DeliveryEventType eventType;
    private Function<Order, Order> deliveryAction = (o) -> order;
    private DeliveryWorkflow deliveryWorkflow;

    public DeliveryEvent() {
        id = (long)UUID.randomUUID().hashCode();
    }

    public DeliveryEvent(Long id, Order order) {
        this.id = id;
        this.order = order;
    }

    public DeliveryEvent(Long id, Order order, DeliveryEventType eventType, Function<Order, Order> deliveryAction) {
        this.id = id;
        this.order = order;
        this.eventType = eventType;
        this.deliveryAction = deliveryAction;
    }

    public DeliveryEvent(Long id) {
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

    public DeliveryEventType getEventType() {
        return eventType;
    }

    public void setEventType(DeliveryEventType eventType) {
        this.eventType = eventType;
    }

    public Function<Order, Order> getDeliveryAction() {
        return deliveryAction;
    }

    public void setDeliveryAction(Function<Order, Order> deliveryAction) {
        this.deliveryAction = deliveryAction;
    }

    public DeliveryWorkflow getDeliveryWorkflow() {
        return deliveryWorkflow;
    }

    public void setDeliveryWorkflow(DeliveryWorkflow deliveryWorkflow) {
        this.deliveryWorkflow = deliveryWorkflow;
    }

    @Override
    public String toString() {
        return "DeliveryEvent{" +
                "id=" + id +
                ", order=" + order +
                ", eventType=" + eventType +
                ", deliveryAction=" + deliveryAction +
                '}';
    }
}
