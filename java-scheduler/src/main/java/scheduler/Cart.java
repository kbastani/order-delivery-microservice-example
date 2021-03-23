package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A cart is an equally sized block of resources that can commit to {@link OrderRequest}. When a cart is delivered to a
 * {@link Track}, the resources inside the {@link Cart} are unpacked in a stream of
 *
 * @param <T>
 */
public class Cart<T> {

    public static final Integer SIZE = Constants.FRAME_SIZE;
    private Long id;
    private final List<OrderRequest<T>> orderRequests;

    public Cart() {
        this.orderRequests = new ArrayList<>();
    }

    public Cart(Long id) {
        this.id = id;
        this.orderRequests = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<OrderRequest<T>> getOrders() {
        return orderRequests;
    }

    /**
     * A scheduler is responsible for committing an order to a cart. Each cart has a delivery time, and
     * when the cart is delivered, it will be unpacked with a function and a collection of resources. The carts
     * are a model of time itself. Each cart represents an arbitrary ratio of time. Each cart is an equal
     * portion of the timeline.
     *
     * @param orderRequest an order describes a commitment of a batch of resources to a sequence of carts
     */
    public void commit(OrderRequest<T> orderRequest) {
        orderRequests.add(orderRequest);
    }

    public Stream<ArrayList<T>> deliver() {
        return orderRequests.stream().map(o -> o.getResource().take(SIZE));
    }
}
