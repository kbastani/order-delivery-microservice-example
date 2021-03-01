package demo.domain;

import demo.event.Event;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity<E extends Event, T extends Serializable> extends Aggregate<E, T> implements Serializable {

    private T identity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<E> events = new ArrayList<>();

    public AbstractEntity() {
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
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
