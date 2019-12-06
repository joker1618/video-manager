package xxx.joker.libs.repo.design.annotation.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The time the Entity is added to the repo.
 * Must be present and can appear only once in the entity declaration.
 * The field marked as CreationTm must be a JkDateTime or a LocalDateTime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CreationTm {

}
