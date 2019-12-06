package xxx.joker.libs.repo.design.annotation.directive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identify an entity with no @EntityPK fields
 * The PK will be the @EntityID
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NoPrimaryKey {

}
