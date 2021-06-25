package demo.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.AbstractEntity;
import demo.domain.Aggregate;
import demo.domain.Command;
import demo.domain.Module;
import demo.order.action.*;
import demo.order.controller.OrderController;
import demo.order.event.OrderEvent;
import demo.restaurant.domain.Restaurant;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Entity(name = "orders")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order extends AbstractEntity<OrderEvent, Long> {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Column
    private Double lat;

    @Column
    private Double lon;

    private Long accountId;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Restaurant restaurant;

    public Order() {
        this.status = OrderStatus.ORDER_CREATED;
    }

    public Order(Long accountId) {
        this();
        this.accountId = accountId;
    }

    @JsonProperty("orderId")
    @Transient
    @Override
    public Long getIdentity() {
        return this.id;
    }

    @Override
    public void setIdentity(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Command(method = "assignOrder", controller = OrderController.class)
    public Order assignOrder(Long restaurantId) {
        return getAction(AssignOrder.class)
                .apply(this, restaurantId);
    }

    @Command(method = "updateOrderLocation", controller = OrderController.class)
    public Order updateOrderLocation(Double lat, Double lon) {
        return getAction(UpdateOrderLocation.class)
                .apply(this, lat, lon);
    }

    @Command(method = "prepareOrder", controller = OrderController.class)
    public Order prepareOrder() {
        return getAction(PrepareOrder.class)
                .apply(this);
    }

    @Command(method = "orderReady", controller = OrderController.class)
    public Order orderReady() {
        return getAction(OrderReady.class)
                .apply(this);
    }

    @Command(method = "orderPickedUp", controller = OrderController.class)
    public Order orderPickedUp() {
        return getAction(OrderPickedUp.class)
                .apply(this);
    }

    @Command(method = "deliverOrder", controller = OrderController.class)
    public Order deliverOrder() {
        return getAction(DeliverOrder.class)
                .apply(this);
    }

    @Command(method = "orderDelivered", controller = OrderController.class)
    public Order orderDelivered() {
        return getAction(OrderDelivered.class)
                .apply(this);
    }

    @Command(method = "updateOrderStatus", controller = OrderController.class)
    public Order updateOrderStatus(OrderStatus orderStatus) {
        return getAction(UpdateOrderStatus.class)
                .apply(this, orderStatus);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<OrderEvent, Long>> T getModule() throws
            IllegalArgumentException {
        OrderModule orderModule = getModule(OrderModule.class);
        return (T) orderModule;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @JsonIgnore
    public Link getId() {
        return linkTo(OrderController.class)
                .slash("orders")
                .slash(getIdentity())
                .withSelfRel();
    }
}
