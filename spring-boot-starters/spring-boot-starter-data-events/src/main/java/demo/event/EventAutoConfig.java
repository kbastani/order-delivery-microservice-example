package demo.event;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * This class auto-configures a {@link BasicEventService} bean.
 *
 * @author Kenny Bastani
 */
@Configuration
@ConditionalOnClass({EventRepository.class, RestTemplate.class})
@ConditionalOnMissingBean(EventService.class)
@EnableConfigurationProperties(EventProperties.class)
public class EventAutoConfig implements BeanFactoryPostProcessor {

    private List<EventRepository> eventRepositories;

    public EventAutoConfig(List<EventRepository> eventRepositories) {
        this.eventRepositories = eventRepositories;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        eventRepositories.forEach(er -> beanFactory
                .registerSingleton(er.getClass().getSuperclass().getSimpleName(), er));
    }
}
