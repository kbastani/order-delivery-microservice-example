package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.domain.Aggregate;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract implementation of the {@link Event} entity.
 *
 * @param <T>  is the entity this {@link Event} applies to
 * @param <E>  is the type of event, typically an {@link Enum}
 * @param <ID> is the unique identifier type used to persist the {@link Event}
 * @author Kenny Bastani
 * @see org.springframework.stereotype.Repository
 * @see RepresentationModel
 */
public abstract class Event<T extends Aggregate, E, ID extends Serializable> extends RepresentationModel {

    public Event() {
    }

    public abstract ID getEventId();

    public abstract void setEventId(ID eventId);

    public abstract E getType();

    public abstract void setType(E type);

    public abstract T getEntity();

    public abstract void setEntity(T entity);

    public abstract Long getAggregateId();

    public abstract void setAggregateId(Long aggregateId);

    public abstract Long getCreatedAt();

    public abstract void setCreatedAt(Long createdAt);

    public abstract Long getLastModified();

    public abstract void setLastModified(Long lastModified);

    @JsonIgnore
    public Link getId() {
        return this.getRequiredLink("self");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Links getLinks() {
        List<Link> links = super.getLinks().stream().collect(Collectors.toList());
        links.add(getId());
        Class<T> clazz = (Class<T>) ((ParameterizedTypeImpl)
                this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        links.add(getEntity().getId().withRel(new EvoInflectorLinkRelationProvider()
                .getItemResourceRelFor(clazz)));
        return Links.of(links);
    }

    @Override
    public String toString() {
        return "Event{} " + super.toString();
    }
}
