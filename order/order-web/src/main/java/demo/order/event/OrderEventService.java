package demo.order.event;

import demo.event.BasicEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderEventService extends BasicEventService<OrderEvent, Long> {
    public OrderEventService(OrderEventRepository eventRepository, RestTemplate restTemplate) {
        super(eventRepository, restTemplate);
    }
}
