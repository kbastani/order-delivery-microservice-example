package demo.order.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import demo.event.Event;
import demo.order.controller.OrderController;
import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Link;

import javax.persistence.*;
import java.util.Objects;

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
@Table(indexes = {@Index(name = "IDX_ORDER_EVENT", columnList = "orderId")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderEvent extends Event<Order, OrderEventType, Long> {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(OrderEvent.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    private Long orderId = 1L;

    @Enumerated(EnumType.STRING)
    private OrderEventType type;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long aggregateId;

    @Column
    private Double orderLocationLat;

    @Column
    private Double orderLocationLon;

    private Long restaurantId;
    private String restaurantName;
    private String restaurantCity;
    private String restaurantCountry;
    private Double restaurantLat;
    private Double restaurantLon;

    @Lob
    @Column(length = 100000)
    private String orderPayload;

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
        this.orderId = entity.getIdentity();
        this.orderStatus = entity.getStatus();
        this.orderLocationLat = entity.getLat();
        this.orderLocationLon = entity.getLon();
        this.aggregateId = entity.getIdentity();

        if (entity.getRestaurant() != null) {
            this.restaurantId = entity.getRestaurant().getStoreId();
            this.restaurantName = entity.getRestaurant().getName();
            this.restaurantCity = entity.getRestaurant().getCity();
            this.restaurantCountry = entity.getRestaurant().getCountry();
            this.restaurantLat = entity.getRestaurant().getLatitude();
            this.restaurantLon = entity.getRestaurant().getLongitude();
        }
        this.setEntity(entity);
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

    @Override
    public Order getEntity() {
        Order result = null;
        if (orderPayload != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
                result = objectMapper.readValue(orderPayload, Order.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing entity payload", e);
            }
        }

        return result;
    }

    @Override
    public void setEntity(Order entity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
            orderPayload = objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Error serializing entity payload", e);
        }
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderPayload() {
        return orderPayload;
    }

    public void setOrderPayload(String orderPayload) {
        this.orderPayload = orderPayload;
    }

    @Override
    public Long getAggregateId() {
        return aggregateId;
    }

    @Override
    public void setAggregateId(Long aggregateId) {
        this.aggregateId = aggregateId;
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

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantCity() {
        return restaurantCity;
    }

    public void setRestaurantCity(String restaurantCity) {
        this.restaurantCity = restaurantCity;
    }

    public String getRestaurantCountry() {
        return restaurantCountry;
    }

    public void setRestaurantCountry(String restaurantCountry) {
        this.restaurantCountry = restaurantCountry;
    }

    public Double getRestaurantLat() {
        return restaurantLat;
    }

    public void setRestaurantLat(Double restaurantLat) {
        this.restaurantLat = restaurantLat;
    }

    public Double getRestaurantLon() {
        return restaurantLon;
    }

    public void setRestaurantLon(Double restaurantLon) {
        this.restaurantLon = restaurantLon;
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
        Link result;

        result = linkTo(OrderController.class).slash("orders")
                .slash(Objects.requireNonNullElse(orderId, 1L)).slash("events")
                .slash(getEventId()).withSelfRel();

        return result;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "eventId=" + eventId +
                ", orderId=" + orderId +
                ", type=" + type +
                ", orderStatus=" + orderStatus +
                ", aggregateId=" + aggregateId +
                ", orderLocationLat=" + orderLocationLat +
                ", orderLocationLon=" + orderLocationLon +
                ", restaurantId=" + restaurantId +
                ", restaurantName='" + restaurantName + '\'' +
                ", restaurantCity='" + restaurantCity + '\'' +
                ", restaurantCountry='" + restaurantCountry + '\'' +
                ", restaurantLat=" + restaurantLat +
                ", restaurantLon=" + restaurantLon +
                ", orderPayload='" + orderPayload + '\'' +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                '}';
    }
}
