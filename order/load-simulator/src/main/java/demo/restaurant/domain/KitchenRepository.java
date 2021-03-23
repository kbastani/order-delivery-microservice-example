package demo.restaurant.domain;

import scheduler.Cart;
import scheduler.OrderRequest;
import scheduler.StreamingRepository;

import java.util.HashMap;
import java.util.Map;

public class KitchenRepository implements StreamingRepository<Cart<OrderDelivery>, OrderDelivery> {

    private final Map<Long, Cart<OrderDelivery>> repo = new HashMap<>();

    @Override
    public Cart<OrderDelivery> getById(Long id) {
        return repo.get(id);
    }

    @Override
    public void save(Cart<OrderDelivery> item) {
        repo.put(item.getId(), item);
    }

    @Override
    public OrderRequest<OrderDelivery> saveOrder(OrderRequest<OrderDelivery> orderRequest) {
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
