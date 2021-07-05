package demo.driver.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.AbstractEntity;
import demo.domain.Aggregate;
import demo.domain.Command;
import demo.domain.Module;
import demo.driver.action.*;
import demo.driver.controller.DriverController;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventType;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(indexes = {@Index(name = "IDX_DRIVER_ID", columnList = "id")})
public class Driver extends AbstractEntity<DriverEvent, Long> {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private DriverAvailabilityStatus availabilityStatus;

    @Enumerated(value = EnumType.STRING)
    private DriverActivityStatus activityStatus;

    @Enumerated(value = EnumType.STRING)
    private DriverStatus driverStatus;

    @Enumerated(value = EnumType.STRING)
    private DriverEventType eventType;

    @Column
    private Double lat;

    @Column
    private Double lon;

    public Driver() {
        driverStatus = DriverStatus.DRIVER_CREATED;
        availabilityStatus = DriverAvailabilityStatus.DRIVER_OFFLINE;
        activityStatus = DriverActivityStatus.ACCOUNT_PENDING;
    }

    @JsonProperty("driverId")
    @Transient
    @Override
    public Long getIdentity() {
        return this.id;
    }

    @Override
    public void setIdentity(Long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DriverAvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(DriverAvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public DriverActivityStatus getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(DriverActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public DriverStatus getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(DriverStatus driverStatus) {
        this.driverStatus = driverStatus;
    }

    public DriverEventType getEventType() {
        return eventType;
    }

    public void setEventType(DriverEventType eventType) {
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

    @Command(method = "updateDriverLocation", controller = DriverController.class)
    public Driver updateDriverLocation(Double lat, Double lon) {
        return getAction(UpdateDriverLocation.class)
                .apply(this, lat, lon);
    }

    @Command(method = "updateDriverStatus", controller = DriverController.class)
    public Driver updateDriverStatus(DriverStatus driverStatus) {
        return getAction(UpdateDriverStatus.class)
                .apply(this, driverStatus);
    }

    @Command(method = "activateAccount", controller = DriverController.class)
    public Driver activateAccount() {
        return getAction(ActivateAccount.class)
                .apply(this);
    }

    @Command(method = "driverOnline", controller = DriverController.class)
    public Driver driverOnline() {
        return getAction(DriverOnline.class)
                .apply(this);
    }

    @Command(method = "driverOffline", controller = DriverController.class)
    public Driver driverOffline() {
        return getAction(DriverOffline.class)
                .apply(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<DriverEvent, Long>> T getModule() throws
            IllegalArgumentException {
        DriverModule driverModule = getModule(DriverModule.class);
        return (T) driverModule;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @JsonIgnore
    public Link getId() {
        return linkTo(DriverController.class)
                .slash("drivers")
                .slash(getIdentity())
                .withSelfRel();
    }
}