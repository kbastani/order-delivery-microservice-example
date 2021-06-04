package scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * A cart is an equally sized block of resources that can commit to {@link ScheduledEvent}. When a cart is delivered to a
 * {@link Track}, the resources inside the {@link Cart} are unpacked in a stream of
 *
 * @param <T>
 */
public class Cart<T> {

    public static final Integer SIZE = Constants.FRAME_SIZE;
    private Long id;
    private final List<ScheduledEvent<T>> scheduledEvents;

    public Cart() {
        this.scheduledEvents = new ArrayList<>();
    }

    public Cart(Long id) {
        this.id = id;
        this.scheduledEvents = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ScheduledEvent<T>> getOrders() {
        return scheduledEvents;
    }

    /**
     * A scheduler is responsible for committing an order to a cart. Each cart has a delivery time, and
     * when the cart is delivered, it will be unpacked with a function and a collection of resources. The carts
     * are a model of time itself. Each cart represents an arbitrary ratio of time. Each cart is an equal
     * portion of the timeline.
     *
     * @param scheduledEvent an order describes a commitment of a batch of resources to a sequence of carts
     */
    public void commit(ScheduledEvent<T> scheduledEvent) {
        scheduledEvents.add(scheduledEvent);
    }

    public Stream<ArrayList<T>> streamMultiChannel() {
        return scheduledEvents.stream().map(o -> o.getResource().take(SIZE));
    }

    public Stream<T> streamSingleChannel() {
        return scheduledEvents.stream().map(o -> o.getResource().take(SIZE))
                .flatMap(Collection::stream);
    }
}
