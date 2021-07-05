package demo.driver.domain;

public class NearbyPreparedOrder {
    private Long orderId;
    private Long preparedAt;
    private Long preparedAge;
    private Double distance;

    public NearbyPreparedOrder() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPreparedAt() {
        return preparedAt;
    }

    public void setPreparedAt(Long preparedAt) {
        this.preparedAt = preparedAt;
    }

    public Long getPreparedAge() {
        return preparedAge;
    }

    public void setPreparedAge(Long preparedAge) {
        this.preparedAge = preparedAge;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "NearbyOrder{" +
                "orderId=" + orderId +
                ", preparedAt=" + preparedAt +
                ", preparedAge=" + preparedAge +
                ", distance=" + distance +
                '}';
    }
}
