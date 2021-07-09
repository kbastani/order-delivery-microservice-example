package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * A track is a lazily-loaded infinite stream of {@link Cart}s that are scheduled on a {@link Track}.
 * The {@link Track} is {@link StreamingRepository} that marshals commitments back and forth between a data store.
 * When a {@link Cart} is delivered, the contents of the {@link Resource}s inside the cart are unpacked into a stream
 * of streams. The result of this stream of streams can be subscribed to by a function that applies aggregate
 * transformations.
 */
public class Track<T> {

    private ExpandingResource<Cart<T>, T> log;
    private Long position = 0L;
    private Long maxPosition = 0L;
    private Integer orderRequests = 0;

    public Track(ExpandingResource<Cart<T>, T> log) {
        this.log = log;
    }

    public void schedule(ScheduledEvent<T> scheduledEvent) {
        if (scheduledEvent.getState() == ResourceState.FULL) {
            orderRequests++;
            // Make sure that the delivery time is in the future
            if (scheduledEvent.getDeliveryTime() < (position)) {
                scheduledEvent.setDeliveryTime(position + 1);
            }
        }

        Cart<T> cart = log.getRepository().getById(scheduledEvent.getDeliveryTime());
        if (cart == null) {
            cart = log.getFactory().apply(scheduledEvent.getDeliveryTime());
        }

        scheduledEvent = log.getRepository().saveOrder(scheduledEvent);
        cart.commit(scheduledEvent);
        log.getRepository().save(cart);
        //maxPosition = maxPosition < order.getDeliveryTime() ? order.getDeliveryTime() : maxPosition;
    }

    public List<T> nextFrame() {
        List<T> result = null;

        if (orderRequests > 0) {
            LinkedBlockingQueue<Cart<T>> results = new LinkedBlockingQueue<>(log.take(1)
                    .stream().collect(Collectors.toUnmodifiableList()));
            Cart<T> item = results.poll();

            //synchronized (Objects.requireNonNull(item)) {
                try {
                    result = item.streamSingleChannel().collect(Collectors.toCollection(ArrayList::new));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            //}

            // Overflow order resources to the next cart until the resource state is exhausted
            item.getOrders().stream().collect(Collectors.toUnmodifiableList()).forEach(o -> {
                if (o.getResource().getState() == ResourceState.EXHAUSTED) {
                    orderRequests--;
                    o.setState(ResourceState.EXHAUSTED);
                } else {
                    o.setState(ResourceState.NOT_EMPTY);
                    o.setDeliveryTime(position + 1);
                    schedule(o);
                }
            });

            // Clean up
            log.getRepository().remove(item.getId());
            position++;
        }

        return result;
    }

    public Long getPosition() {
        return position;
    }

    public Long getMaxPosition() {
        return maxPosition;
    }

    public boolean isEmpty() {
        return log.getRepository().isEmpty();
    }

    public Integer getOrders() {
        return orderRequests;
    }

    @Override
    public String toString() {
        return "Track{" +
                "log=" + log +
                ", position=" + position +
                ", maxPosition=" + maxPosition +
                ", orderRequests=" + orderRequests +
                '}';
    }
}
