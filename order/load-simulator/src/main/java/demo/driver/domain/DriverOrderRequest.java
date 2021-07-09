package demo.driver.domain;

public class DriverOrderRequest {

    private Long orderId;
    private Long restaurantId;
    private double restaurantLat;
    private double restaurantLon;
    private double deliveryLat;
    private double deliveryLon;
    private double estimatedPayment;

    public DriverOrderRequest() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public double getRestaurantLat() {
        return restaurantLat;
    }

    public void setRestaurantLat(double restaurantLat) {
        this.restaurantLat = restaurantLat;
    }

    public double getRestaurantLon() {
        return restaurantLon;
    }

    public void setRestaurantLon(double restaurantLon) {
        this.restaurantLon = restaurantLon;
    }

    public double getDeliveryLat() {
        return deliveryLat;
    }

    public void setDeliveryLat(double deliveryLat) {
        this.deliveryLat = deliveryLat;
    }

    public double getDeliveryLon() {
        return deliveryLon;
    }

    public void setDeliveryLon(double deliveryLon) {
        this.deliveryLon = deliveryLon;
    }

    public double getEstimatedPayment() {
        return estimatedPayment;
    }

    public void setEstimatedPayment(double estimatedPayment) {
        this.estimatedPayment = estimatedPayment;
    }

    @Override
    public String toString() {
        return "DriverOrderRequest{" +
                "orderId=" + orderId +
                ", restaurantId=" + restaurantId +
                ", restaurantLat=" + restaurantLat +
                ", restaurantLon=" + restaurantLon +
                ", deliveryLat=" + deliveryLat +
                ", deliveryLon=" + deliveryLon +
                ", estimatedPayment=" + estimatedPayment +
                '}';
    }
}
