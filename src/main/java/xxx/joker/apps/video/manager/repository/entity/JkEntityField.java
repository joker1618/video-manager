package xxx.joker.apps.video.manager.repository.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})

/**
 * ALLOWED FIELD TYPES:
 *
 * 	boolean.class 	Boolean.class
 * 	int.class     	Integer.class
 * 	long.class    	Long.class
 * 	float.class   	Float.class
 * 	double.class  	Double.class
 *
 * 	File.class
 * 	Path.class
 * 	LocalTime.class
 * 	LocalDate.class
 * 	LocalDateTime.class
 * 	String.class
 *
 * 	? extends JkEntity
 *
 * ALLOWED COLLECTION TYPES:   (elements class type must be one of simple allowed above)
 * 	- List
 * 	- Set
 *
 * PARAMETERS:
 * - collectionType: must be specified for 'List' and 'Set' fields. Must be one of the classes specified above.
 *
 * DETAILS:
 * - String  -->  null not permitted: used ""
 * - List    -->  null not permitted: used 'emptyList'
 * - Set     -->  null not permitted: used 'emptySet'
 *
 */

public @interface JkEntityField {

    int index();

    // Must be specified only for Collections (List, Set)
	Class<?> collectionType() default Object.class;

}
