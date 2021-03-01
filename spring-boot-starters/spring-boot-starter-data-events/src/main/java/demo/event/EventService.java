package demo.event;

import demo.domain.Aggregate;
import org.springframework.hateoas.Link;

import java.io.Serializable;

/**
 * Service interface for managing {@link Event} entities.
 *
 * @author Kenny Bastani
 * @see Event
 * @see Events
 * @see BasicEventService
 */
public interface EventService<T extends Event, ID extends Serializable> {

    /**
     * Raises a synchronous domain event. An {@link Event} will be applied to an entity through a chain of HTTP
     * requests/responses.
     *
     * @return the applied {@link Event}
     */
    <E extends Aggregate, S extends T> S send(S event, Link... links);

    /**
     * Raises an asynchronous domain event. An {@link Event} will be applied to an entity through a chain of AMQP
     * messages.
     *
     * @return a flag indicating if the {@link Event} message was sent successfully
     */
    <S extends T> Boolean sendAsync(S event, Link... links);

    /**
     * Saves a given event entity. Use the returned instance for further operations as the save operation might have
     * changed the entity instance completely.
     *
     * @return the saved event entity
     */
    <S extends T> S save(S event);

    /**
     * Saves a given event entity. Use the returned instance for further operations as the save operation might have
     * changed the entity instance completely. The {@link ID} parameter is the unique {@link Event} identifier.
     *
     * @return the saved event entity
     */
    <S extends T> S save(ID id, S event);

    /**
     * Retrieves an {@link Event} entity by its id.
     *
     * @return the {@link Event} entity with the given id or {@literal null} if none found
     */
    <EID extends ID> T findOne(EID id);

    /**
     * Retrieves an entity's {@link Event}s by its id.
     *
     * @return a {@link Events} containing a collection of {@link Event}s
     */
    <E extends Events> E find(ID entityId);
}
