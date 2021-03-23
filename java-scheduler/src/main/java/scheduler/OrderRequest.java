package scheduler;

/**
 * An order is a container of a variable-length resource that has been committed to a sequence of {@link Cart}s.
 */
public class OrderRequest<T> {

    private Long id;
    private Long deliveryTime = 0L;
    private Resource<T> resource;
    private ResourceState state;

    public OrderRequest() {
        state = ResourceState.FULL;
    }

    public OrderRequest(Long id, Resource<T> resource) {
        this();
        this.id = id;
        this.resource = resource;
    }

    public OrderRequest(Long id, Long deliveryTime, Resource<T> resource) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderRequest<?> orderRequest = (OrderRequest<?>) o;

        if (id != null ? !id.equals(orderRequest.id) : orderRequest.id != null) return false;
        if (deliveryTime != null ? !deliveryTime.equals(orderRequest.deliveryTime) : orderRequest.deliveryTime != null) return false;
        if (resource != null ? !resource.equals(orderRequest.resource) : orderRequest.resource != null) return false;
        return state == orderRequest.state;
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
        return "Order{" +
                "id=" + id +
                ", deliveryTime=" + deliveryTime +
                ", resource=" + resource +
                ", state=" + state +
                '}';
    }
}
