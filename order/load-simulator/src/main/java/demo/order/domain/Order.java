package demo.order.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    private Long orderId;
    private OrderStatus status;
    private Long createdAt;
    private Long lastModified;
    private Double lat;
    private Double lon;
    private Double customerLat;
    private Double customerLon;

    private Long accountId, restaurantId;

    public Order() {
        this.status = OrderStatus.ORDER_CREATED;
    }

    public Order(Long accountId) {
        this();
        this.accountId = accountId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
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

    public Double getCustomerLat() {
        return customerLat;
    }

    public void setCustomerLat(Double customerLat) {
        this.customerLat = customerLat;
    }

    public Double getCustomerLon() {
        return customerLon;
    }

    public void setCustomerLon(Double customerLon) {
        this.customerLon = customerLon;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                ", lat=" + lat +
                ", lon=" + lon +
                ", accountId=" + accountId +
                ", restaurantId=" + restaurantId +
                '}';
    }
}
