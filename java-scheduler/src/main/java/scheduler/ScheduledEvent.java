package scheduler;

import java.util.function.Consumer;

/**
 * An order is a container of a variable-length resource that has been committed to a sequence of {@link Cart}s.
 */
public class ScheduledEvent<T> {

    private Long id;
    private Long deliveryTime = 0L;
    private Consumer<ScheduledEvent<T>> updateDeliveryTime = (event) -> {};
    private Resource<T> resource;
    private ResourceState state;

    public ScheduledEvent() {
        state = ResourceState.FULL;
    }

    public ScheduledEvent(Long id, Resource<T> resource) {
        this();
        this.id = id;
        this.resource = resource;
    }

    public ScheduledEvent(Long id, Resource<T> resource, Consumer<ScheduledEvent<T>> updateDeliveryTime) {
        this();
        this.id = id;
        this.resource = resource;
        this.updateDeliveryTime = updateDeliveryTime;
    }

    public ScheduledEvent(Long id, Long deliveryTime, Resource<T> resource) {
        this();
        this.id = id;
        this.deliveryTime = deliveryTime;
        this.resource = resource;
    }

    public Long getId() {
        return id;
    }

    public Resource<T> getResource() {
        return resource;
    }

    public Long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public ResourceState getState() {
        return state;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }

    public Consumer<ScheduledEvent<T>> getUpdateDeliveryTime() {
        return updateDeliveryTime;
    }

    public void setUpdateDeliveryTime(Consumer<ScheduledEvent<T>> updateDeliveryTime) {
        this.updateDeliveryTime = updateDeliveryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledEvent<?> scheduledEvent = (ScheduledEvent<?>) o;

        if (id != null ? !id.equals(scheduledEvent.id) : scheduledEvent.id != null) return false;
        if (deliveryTime != null ? !deliveryTime.equals(scheduledEvent.deliveryTime) : scheduledEvent.deliveryTime != null) return false;
        if (resource != null ? !resource.equals(scheduledEvent.resource) : scheduledEvent.resource != null) return false;
        return state == scheduledEvent.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (deliveryTime != null ? deliveryTime.hashCode() : 0);
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScheduledEvent{" +
                "id=" + id +
                ", deliveryTime=" + deliveryTime +
                ", resource=" + resource +
                ", state=" + state +
                '}';
    }
}
