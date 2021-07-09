package demo.driver.domain;

public class DriverProperties {

    private Long newDriverTime = 15000L;
    private Long preparationTime = 3000L;
    private Double preparationRate = 4.0;

    public DriverProperties() {
    }

    public DriverProperties(Long newDriverTime) {
        this.newDriverTime = newDriverTime;
    }

    public DriverProperties(Long newDriverTime, Long preparationTime) {
        this.newDriverTime = newDriverTime;
        this.preparationTime = preparationTime;
    }

    public DriverProperties(Long newDriverTime, Long preparationTime, Double preparationRate) {
        this.newDriverTime = newDriverTime;
        this.preparationTime = preparationTime;
        this.preparationRate = preparationRate;
    }

    public Long getNewDriverTime() {
        return newDriverTime;
    }

    public void setNewDriverTime(Long newDriverTime) {
        this.newDriverTime = newDriverTime;
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
