package demo.driver.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import demo.driver.controller.DriverController;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverStatus;
import demo.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Link;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * The domain event {@link DriverEvent} tracks the type and state of events as applied to the {@link Driver} domain
 * object. This event resource can be used to event source the aggregate state of {@link Driver}.
 * <p>
 * This event resource also provides a transaction log that can be used to append actions to the event.
 *
 * @author Kenny Bastani
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {@Index(name = "IDX_DRIVER_EVENT", columnList = "driverId")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverEvent extends Event<Driver, DriverEventType, Long> {

    @Transient
    @JsonIgnore
    private final Logger log = LoggerFactory.getLogger(DriverEvent.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    private Long driverId = 1L;

    @Enumerated(EnumType.STRING)
    private DriverEventType type;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus;

    private Long aggregateId;

    private String aggregateType = "Driver";

    @Column
    private Double driverLocationLat;

    @Column
    private Double driverLocationLon;

    @Lob
    @Column(length = 100000)
    private String driverPayload;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    public DriverEvent() {
    }

    public DriverEvent(DriverEventType type) {
        this.type = type;
    }

    public DriverEvent(DriverEventType type, Driver entity) {
        this.type = type;
        this.driverId = entity.getIdentity();
        this.driverStatus = entity.getDriverStatus();
        this.driverLocationLat = entity.getLat();
        this.driverLocationLon = entity.getLon();
        this.aggregateId = entity.getIdentity();
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
    public DriverEventType getType() {
        return type;
    }

    @Override
    public void setType(DriverEventType type) {
        this.type = type;
    }

    @Override
    public Driver getEntity() {
        Driver result = null;
        if (driverPayload != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper()
                        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
                result = objectMapper.readValue(driverPayload, Driver.class);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing entity payload", e);
            }
        }

        return result;
    }

    @Override
    public void setEntity(Driver entity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            driverPayload = objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Error serializing entity payload", e);
        }
    }

    public DriverStatus getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(DriverStatus driverStatus) {
        this.driverStatus = driverStatus;
    }

    public String getDriverPayload() {
        return driverPayload;
    }

    public void setDriverPayload(String driverPayload) {
        this.driverPayload = driverPayload;
    }

    @Override
    public Long getAggregateId() {
        return aggregateId;
    }

    @Override
    public void setAggregateId(Long aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Double getDriverLocationLat() {
        return driverLocationLat;
    }

    public void setDriverLocationLat(Double driverLocationLat) {
        this.driverLocationLat = driverLocationLat;
    }

    public Double getDriverLocationLon() {
        return driverLocationLon;
    }

    public void setDriverLocationLon(Double driverLocationLon) {
        this.driverLocationLon = driverLocationLon;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    @Override
    public Link getId() {
        Link result;

        result = linkTo(DriverController.class).slash("drivers")
                .slash(Objects.requireNonNullElse(driverId, 1L)).slash("events")
                .slash(getEventId()).withSelfRel();

        return result;
    }

    @Override
    public String toString() {
        return "DriverEvent{" +
                "eventId=" + eventId +
                ", driverId=" + driverId +
                ", type=" + type +
                ", driverStatus=" + driverStatus +
                ", aggregateId=" + aggregateId +
                ", aggregateType='" + aggregateType + '\'' +
                ", driverLocationLat=" + driverLocationLat +
                ", driverLocationLon=" + driverLocationLon +
                ", driverPayload='" + driverPayload + '\'' +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                "} " + super.toString();
    }
}
