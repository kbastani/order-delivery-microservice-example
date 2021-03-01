package demo.domain;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;

/**
 * A {@link Service} is a functional unit that provides a need. Services are immutable and often stateless. Services
 * always consume or produce {@link Aggregate} objects. Services are addressable and discoverable by other services.
 *
 * @author Kenny Bastani
 */
@org.springframework.stereotype.Service
public abstract class Service<T extends Aggregate, ID extends Serializable> implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public abstract T get(ID id);
    public abstract T create(T entity);
    public abstract T update(T entity);
    public abstract boolean delete(ID id);

    @SuppressWarnings("unchecked")
    public <A extends Action<T>> A getAction(Class<? extends A> clazz) {
        return applicationContext.getBean(clazz);
    }
}
