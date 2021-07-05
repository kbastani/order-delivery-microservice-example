package demo.domain;

import demo.event.Event;

import java.io.Serializable;
import java.util.Date;

public class EmptyEvent extends Event {
    @Override
    public Serializable getEventId() {
        return null;
    }

    @Override
    public void setEventId(Serializable eventId) {

    }

    @Override
    public Object getType() {
        return null;
    }

    @Override
    public void setType(Object type) {

    }

    @Override
    public Aggregate getEntity() {
        return null;
    }

    @Override
    public void setEntity(Aggregate entity) {

    }

    @Override
    public Serializable getAggregateId() {
        return null;
    }

    @Override
    public void setAggregateId(Serializable aggregateId) {

    }

    @Override
    public Date getCreatedAt() {
        return null;
    }

    @Override
    public void setCreatedAt(Date createdAt) {

    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public void setLastModified(Date lastModified) {

    }


}
