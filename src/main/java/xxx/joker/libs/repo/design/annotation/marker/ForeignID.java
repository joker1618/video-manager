package xxx.joker.libs.repo.design.annotation.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field that store an ID of another entity.
 * Cannot be used as primary key field.
 *
 * Can be used to create a relationship between entities, without have a circular dependency.
 * ex/
 *      A use B
 *      B as a field that contains the ID of A
 *
 * The field type must be Long.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ForeignID {

}
