package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.domain.Aggregate;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import java.io.Serializable;
import java.util.List;

/**
 * General helper to easily create a wrapper for a collection of {@link Event} entities.
 *
 * @author Kenny Bastani
 */
public class Events<T extends Aggregate, E, ID extends Serializable> extends Resources<Event<T, E, ID>> {

    private ID entityId;

    public Events(ID entityId, List<Event<T, E, ID>> content) {
        this(content);
        this.entityId = entityId;
    }

    public Events(Iterable<Event<T, E, ID>> content, Link... links) {
        super(content, links);
    }

    @JsonIgnore
    public ID getEntityId() {
        return entityId;
    }
}
