package demo.domain;


import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

/**
 * {@link Entity} objects are wrappers that contain the serializable properties that uniquely identify an entity.
 * Entities contain a collection of relationships. Entities contain a collection of comparison operators.
 * The default identity comparator evaluates true if the compared objects have the same identifier.
 *
 * @author Kenny Bastani
 */
public interface Entity<ID extends Serializable> extends Identifiable<ID> {
}
