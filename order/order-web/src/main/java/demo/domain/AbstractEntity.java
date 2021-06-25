package demo.domain;

import demo.event.Event;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity<E extends Event, T extends Serializable> extends Aggregate<E, T> implements Serializable {

    @Transient
    private T identity;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<E> events = new ArrayList<>();

    public AbstractEntity() {
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
    public List<E> getEvents() {
        return events;
    }

    public void setEvents(List<E> events) {
        this.events = events;
    }

    @Override
    public T getIdentity() {
        return identity;
    }

    public void setIdentity(T id) {
        this.identity = id;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                '}';
    }
}
