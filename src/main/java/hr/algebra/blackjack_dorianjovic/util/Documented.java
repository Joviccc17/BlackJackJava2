package hr.algebra.blackjack_dorianjovic.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark important business logic methods.
 * Used by the ReflectionDocGenerator to highlight key methods in documentation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Documented {

    /**
     * Description of what this method does in the business logic.
     */
    String description();
}

