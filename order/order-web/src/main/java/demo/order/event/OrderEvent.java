package demo.order.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import demo.event.Event;
import demo.order.controller.OrderController;
import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * The domain event {@link OrderEvent} tracks the type and state of events as applied to the {@link Order} domain
 * object. This event resource can be used to event source the aggregate state of {@link Order}.
 * <p>
 * This event resource also provides a transaction log that can be used to append actions to the event.
 *
 * @author Kenny Bastani
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {@Index(name = "IDX_ORDER_EVENT", columnList = "entity_id")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderEvent extends Event<Order, OrderEventType, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private OrderEventType type;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long primaryKey;

    private Double orderLocationLat;
    private Double orderLocationLon;
    private Long restaurantId;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    private Order entity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;

    public OrderEvent() {
    }

    public OrderEvent(OrderEventType type) {
        this.type = type;
    }

    public OrderEvent(OrderEventType type, Order entity) {
        this.type = type;
        this.entity = entity;
        this.orderId = entity.getIdentity();
        this.orderStatus = entity.getStatus();
        this.restaurantId = entity.getRestaurantId();
        this.orderLocationLat = entity.getLat();
        this.orderLocationLon = entity.getLon();
        this.primaryKey = entity.getIdentity();
    }

    @Override
    public Long getEventId() {
        return eventId;
    }

    @Override
    public void setEventId(Long id) {
        eventId = id;
    }

    @Override
    public OrderEventType getType() {
        return type;
    }

    @Override
    public void setType(OrderEventType type) {
        this.type = type;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public Order getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Order entity) {
        this.entity = entity;
    }

    @Override
    public Long getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public void setPrimaryKey(Long primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Double getOrderLocationLat() {
        return orderLocationLat;
    }

    public void setOrderLocationLat(Double orderLocationLat) {
        this.orderLocationLat = orderLocationLat;
    }

    public Double getOrderLocationLon() {
        return orderLocationLon;
    }

    public void setOrderLocationLon(Double orderLocationLon) {
        this.orderLocationLon = orderLocationLon;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public Link getId() {
        return linkTo(OrderController.class).slash("orders")
                .slash(getEntity().getIdentity()).slash("events")
                .slash(getEventId()).withSelfRel();
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "eventId=" + eventId +
                ", type=" + type +
                ", entity=" + entity +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                "} " + super.toString();
    }
}
