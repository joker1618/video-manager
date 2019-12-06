package xxx.joker.libs.repo.design.annotation.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identify the fields to be used as a primary key.
 * The fields are joined together as a string in the order they appears in the class.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EntityPK {

}
