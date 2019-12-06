package xxx.joker.libs.repo.design.annotation.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field used as repo UID.
 * Constraints:
 * - must be present and can appear only once in the entity declaration
 * - must be a Long
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EntityID {

}
