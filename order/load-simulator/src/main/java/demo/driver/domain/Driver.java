package demo.driver.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.order.domain.Order;

public class Driver {
    private String createdAt;
    private String lastModified;
    private String availabilityStatus;
    private String activityStatus;
    private String driverStatus;
    private String eventType;
    private Double lat;
    private Double lon;
    private Long driverId;
    private Long orderId;

    @JsonIgnore
    private Order order;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(String activityStatus) {
        this.activityStatus = activityStatus;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "createdAt='" + createdAt + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                ", activityStatus='" + activityStatus + '\'' +
                ", driverStatus='" + driverStatus + '\'' +
                ", eventType='" + eventType + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", driverId=" + driverId +
                ", orderId=" + orderId +
                ", order=" + order +
                '}';
    }
}
