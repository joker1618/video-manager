package xxx.joker.libs.repo.design.annotation.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EntityField {

    /**
     * if refType == RepoConfig.REF_TYPE_FIELD_RESOURCE_PATH
     * - must be a Path
     * - is persisted as relative path  (from the resources folder)
     * - the field value will be an absolute path  (starting with ${resources_folder})
     */
    String refType() default "";

}
