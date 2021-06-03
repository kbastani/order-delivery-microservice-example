package demo.restaurant.config;

public class RestaurantProperties {

    private Long newOrderTime = 15000L;
    private Long preparationTime = 3000L;
    private Double preparationRate = 4.0;

    public RestaurantProperties() {
    }

    public RestaurantProperties(Long newOrderTime) {
        this.newOrderTime = newOrderTime;
    }

    public RestaurantProperties(Long newOrderTime, Long preparationTime) {
        this.newOrderTime = newOrderTime;
        this.preparationTime = preparationTime;
    }

    public RestaurantProperties(Long newOrderTime, Long preparationTime, Double preparationRate) {
        this.newOrderTime = newOrderTime;
        this.preparationTime = preparationTime;
        this.preparationRate = preparationRate;
    }

    public Long getNewOrderTime() {
        return newOrderTime;
    }

    public void setNewOrderTime(Long newOrderTime) {
        this.newOrderTime = newOrderTime;
    }

    public Long getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Long preparationTime) {
        this.preparationTime = preparationTime;
    }

    public Double getPreparationRate() {
        return preparationRate;
    }

    public void setPreparationRate(Double preparationRate) {
        this.preparationRate = preparationRate;
    }
}
