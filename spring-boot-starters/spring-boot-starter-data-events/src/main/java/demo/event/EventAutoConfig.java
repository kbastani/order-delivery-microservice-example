package demo.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class auto-configures a {@link BasicEventService} bean.
 *
 * @author Kenny Bastani
 */
@Configuration
@ConditionalOnClass({EventRepository.class, Source.class, RestTemplate.class})
@ConditionalOnMissingBean(EventService.class)
@EnableConfigurationProperties(EventProperties.class)
public class EventAutoConfig {

    private EventRepository eventRepository;
    private RestTemplate restTemplate;

    public EventAutoConfig(EventRepository eventRepository, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Bean
    public EventService eventService(EventSource eventSource) {
        return new BasicEventService(eventRepository, eventSource, restTemplate);
    }

    @Bean
    public EventSource eventSource(Source source) {
        return new EventSource(source.output());
    }
}
