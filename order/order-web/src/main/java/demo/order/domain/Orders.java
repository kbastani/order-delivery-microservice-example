package demo.order.domain;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

public class Orders extends CollectionModel<Order> {

    /**
     * Creates an empty {@link CollectionModel} instance.
     */
    public Orders() {
    }

    /**
     * Creates a {@link CollectionModel} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link CollectionModel}.
     */
    public Orders(Iterable<Order> content, Link... links) {
        super(content, links);
    }
}
