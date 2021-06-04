package demo.restaurant.domain;

import scheduler.Cart;
import scheduler.ScheduledEvent;
import scheduler.StreamingRepository;

import java.util.HashMap;
import java.util.Map;

public class DeliveryRepository implements StreamingRepository<Cart<DeliveryEvent>, DeliveryEvent> {

    private final Map<Long, Cart<DeliveryEvent>> repo = new HashMap<>();

    @Override
    public Cart<DeliveryEvent> getById(Long id) {
        return repo.get(id);
    }

    @Override
    public void save(Cart<DeliveryEvent> item) {
        repo.put(item.getId(), item);
    }

    @Override
    public ScheduledEvent<DeliveryEvent> saveOrder(ScheduledEvent<DeliveryEvent> scheduledEvent) {
        return scheduledEvent;
    }

    @Override
    public void remove(Long id) {
        repo.remove(id);
    }

    @Override
    public boolean isEmpty() {
        return repo.isEmpty();
    }
}
