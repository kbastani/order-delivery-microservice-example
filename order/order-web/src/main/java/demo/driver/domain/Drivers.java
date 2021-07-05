package demo.driver.domain;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;

public class Drivers extends CollectionModel<Driver> {

    /**
     * Creates an empty {@link CollectionModel} instance.
     */
    public Drivers() {
    }

    /**
     * Creates a {@link CollectionModel} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link CollectionModel}.
     */
    public Drivers(Iterable<Driver> content, Link... links) {
        super(content, links);
    }
}
