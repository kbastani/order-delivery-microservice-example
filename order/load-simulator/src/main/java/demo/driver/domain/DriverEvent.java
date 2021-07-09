package demo.driver.domain;

import java.util.UUID;
import java.util.function.Function;

public class DriverEvent {

    private final Long id;
    private Driver driver;
    private DriverEventType eventType;
    private Function<Driver, Driver> driverAction = (o) -> driver;
    private DriverWorkflow driverWorkflow;

    public DriverEvent() {
        id = (long)UUID.randomUUID().hashCode();
    }

    public DriverEvent(Long id, Driver driver) {
        this.id = id;
        this.driver = driver;
    }

    public DriverEvent(Long id, Driver driver, DriverEventType eventType, Function<Driver, Driver> driverAction) {
        this.id = id;
        this.eventType = eventType;
        this.driverAction = driverAction;
        this.driver = driver;
    }

    public DriverEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public DriverEventType getEventType() {
        return eventType;
    }

    public void setEventType(DriverEventType eventType) {
        this.eventType = eventType;
    }

    public Function<Driver, Driver> getDriverAction() {
        return driverAction;
    }

    public void setDriverAction(Function<Driver, Driver> driverAction) {
        this.driverAction = driverAction;
    }

    public DriverWorkflow getDriverWorkflow() {
        return driverWorkflow;
    }

    public void setDriverWorkflow(DriverWorkflow driverWorkflow) {
        this.driverWorkflow = driverWorkflow;
    }

    @Override
    public String toString() {
        return "DriverEvent{" +
                "id=" + id +
                ", driver=" + driver +
                ", eventType=" + eventType +
                ", driverAction=" + driverAction +
                '}';
    }
}
