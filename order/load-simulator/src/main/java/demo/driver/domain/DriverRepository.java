package demo.driver.domain;

import scheduler.Cart;
import scheduler.ScheduledEvent;
import scheduler.StreamingRepository;

import java.util.HashMap;
import java.util.Map;

public class DriverRepository implements StreamingRepository<Cart<DriverEvent>, DriverEvent> {

    private final Map<Long, Cart<DriverEvent>> repo = new HashMap<>();

    @Override
    public Cart<DriverEvent> getById(Long id) {
        return repo.get(id);
    }

    @Override
    public void save(Cart<DriverEvent> item) {
        repo.put(item.getId(), item);
    }

    @Override
    public ScheduledEvent<DriverEvent> saveOrder(ScheduledEvent<DriverEvent> scheduledEvent) {
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
