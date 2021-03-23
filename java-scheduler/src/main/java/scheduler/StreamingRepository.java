package scheduler;

public interface StreamingRepository<T, V> {
    T getById(Long id);
    void save(T t);
    OrderRequest<V> saveOrder(OrderRequest<V> orderRequest);
    void remove(Long id);
    boolean isEmpty();
}
