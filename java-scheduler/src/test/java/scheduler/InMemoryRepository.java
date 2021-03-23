package scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository implements StreamingRepository<Cart<Integer>, Integer> {

    private final Map<Long, Cart<Integer>> repo =  new ConcurrentHashMap<>();

    @Override
    public Cart<Integer> getById(Long id) {
        return repo.get(id);
    }

    @Override
    public void save(Cart<Integer> item) {
        repo.put(item.getId(), item);
    }

    @Override
    public OrderRequest<Integer> saveOrder(OrderRequest<Integer> orderRequest) {
        return orderRequest;
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
