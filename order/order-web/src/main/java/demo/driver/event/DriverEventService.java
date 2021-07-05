package demo.driver.event;

import demo.event.BasicEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DriverEventService extends BasicEventService<DriverEvent, Long> {
    public DriverEventService(DriverEventRepository eventRepository, RestTemplate restTemplate) {
        super(eventRepository, restTemplate);
    }
}
