package demo.domain;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * A {@link Command} is an annotated method that contains a reference to a function in the context of an
 * {@link Aggregate}. A command maps a method reference on an {@link Aggregate} to a function invocation. Commands
 * are discoverable.
 *
 * @author Kenny Bastani
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Command {

    String description() default "";

    @Required
    String method() default "";

    @Required
    Class controller();
}
