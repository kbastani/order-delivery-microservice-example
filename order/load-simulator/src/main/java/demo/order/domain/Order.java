package demo.order.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    private Long orderId;
    private OrderStatus status;
    private Date createdAt;
    private Date lastModified;
    private Double lat;
    private Double lon;
    private Double customerLat;
    private Double customerLon;
    private Long accountId;

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
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
                ", customerLat=" + customerLat +
                ", customerLon=" + customerLon +
                ", accountId=" + accountId +
                '}';
    }
}
