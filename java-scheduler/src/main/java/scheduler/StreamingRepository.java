package scheduler;

public interface StreamingRepository<T, V> {
    T getById(Long id);
    void save(T t);
    ScheduledEvent<V> saveOrder(ScheduledEvent<V> scheduledEvent);
    void remove(Long id);
    boolean isEmpty();
}
