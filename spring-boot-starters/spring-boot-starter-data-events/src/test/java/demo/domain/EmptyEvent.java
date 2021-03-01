package demo.domain;

import demo.event.Event;

import java.io.Serializable;

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
    public Long getCreatedAt() {
        return null;
    }

    @Override
    public void setCreatedAt(Long createdAt) {

    }

    @Override
    public Long getLastModified() {
        return null;
    }

    @Override
    public void setLastModified(Long lastModified) {

    }
}
