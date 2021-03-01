package demo.order.event;

import demo.event.EventRepository;

public interface OrderEventRepository extends EventRepository<OrderEvent, Long> {
}
