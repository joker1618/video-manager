package xxx.joker.libs.core.runtime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.format.JkFormatter;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static xxx.joker.libs.core.util.JkConvert.toList;

public class JkReflection {

	public static <T> T createInstance(String clazzName) {
		try {
			return (T) createInstance(Class.forName(clazzName));
		} catch (Exception e) {
			throw new JkRuntimeException(e, "Error creating instance of {}", clazzName);
		}
	}
	public static <T> T createInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new JkRuntimeException(e, "Error creating instance of {}", clazz);
		}
	}

	public static void setFieldValue(Object instance, String fieldName, Object value) {
		setFieldValue(instance, getFieldByName(instance.getClass(), fieldName), value);
	}
	public static void setFieldValue(Object instance, Field field, Object value) {
		try {
			if(field.isAccessible()) {
				field.set(instance, value);
			} else {
				field.setAccessible(true);
				field.set(instance, value);
				field.setAccessible(false);
			}
		} catch (Exception e) {
			throw new JkRuntimeException(e, "Class {}: error setting value ({}) for field ({}) ", instance.getClass(), value, field.getName());
		}
	}

	public static <T> T getFieldValue(Object instance, String fieldName) {
		return getFieldValue(instance, getFieldByName(instance.getClass(), fieldName));
	}
	public static <T> T getFieldValue(Object instance, Field field) {
		try {
			Object obj;
			if (field.isAccessible()) {
				obj = field.get(instance);
			} else {
				field.setAccessible(true);
				obj = field.get(instance);
				field.setAccessible(false);
			}
			return (T) obj;

		} catch (Exception ex) {
			throw new JkRuntimeException(ex, "Class {}: error getting value from field ({})", ToStringBuilder.reflectionToString(instance), field.getName());
//			throw new JkRuntimeException(ex, "Class {}: error getting value from field ({})", instance.getClass().getName(), field.getName());
		}
	}

	public static Field getFieldByAnnotation(Class<?> sourceClass, Class<? extends Annotation> annotationClass) {
		List<Field> toRet = getFieldsByAnnotation(sourceClass, annotationClass);
		return toRet.isEmpty() ? null : toRet.get(0);
	}
	public static List<Field> getFieldsByAnnotation(Class<?> sourceClass, Class<? extends Annotation> annotationClass) {
		return JkStreams.filter(findAllFields(sourceClass), f -> f.getAnnotation(annotationClass) != null);
	}
	public static List<Field> getFieldsByType(Class<?> sourceClass, Class<?> fieldType) {
		return JkStreams.filter(findAllFields(sourceClass), f -> isInstanceOf(f.getType(), fieldType));
	}
	public static Field getFieldByName(Class<?> sourceClass, String fieldName) {
		return JkStreams.findUnique(findAllFields(sourceClass), f -> f.getName().equals(fieldName));
	}

	public static Enum getEnumByName(Class<?> enumClass, String enumName) {
		Enum[] enumConstants = (Enum[]) enumClass.getEnumConstants();
		for(Enum elem : enumConstants) {
			if(elem.name().equals(enumName)) {
				return elem;
			}
		}
		return null;
	}

	public static boolean isInstanceOf(Class<?> clazz, Collection<Class<?>> expected) {
		List<Class<?>> types = findAllClassTypes(clazz);
		for(Class<?> cexp : expected) {
			if(types.contains(cexp)) {
				return true;
			}
		}
		return false;
	}
	public static boolean isInstanceOf(Class<?> clazz, Class<?>... expected) {
		return isInstanceOf(clazz, Arrays.asList(expected));
	}
	public static boolean isOfClass(Class<?> clazz, Collection<Class<?>> classes) {
		for(Class<?> c : classes) {
			if(c == clazz) {
				return true;
			}
		}
		return false;
	}
	public static boolean isOfClass(Class<?> clazz, Class<?>... classes) {
		return isOfClass(clazz, toList(classes));
	}

	public static List<Class<?>> findAllClassTypes(Class<?> clazz) {
		Set<Class<?>> types = new HashSet<>();

		Class<?> tmp = clazz;
		while(tmp != null) {
			types.add(tmp);
			types.addAll(findAllInterfaces(tmp));
			tmp = tmp.getSuperclass();
		}

		return new ArrayList<>(types);
	}

	private static Set<Class<?>> findAllInterfaces(Class<?> clazz) {
		Set<Class<?>> interfaces = new HashSet<>();
		if(clazz.isInterface()) {
			interfaces.add(clazz);
		}
		for(Class<?> interf : clazz.getInterfaces()) {
			interfaces.addAll(findAllInterfaces(interf));
		}
		if(clazz.getSuperclass() != null) {
			findAllInterfaces(clazz.getSuperclass());
		}
		return interfaces;
	}

	public static List<Field> findAllFields(Class<?> clazz) {
		Class<?> sourceClazz = clazz;
		List<Field> fields = new ArrayList<>();
		while(sourceClazz != null) {
			fields.addAll(0, toList(sourceClazz.getDeclaredFields()));
			sourceClazz = sourceClazz.getSuperclass();
		}
		return fields;
	}

	public static Class<?>[] getParametrizedTypes(Class<?> clazz, String fieldName) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			return getParametrizedTypes(field);
		} catch (NoSuchFieldException e) {
			return new Class<?>[0];
		}
	}
	public static Class<?>[] getParametrizedTypes(Field field) {
		if(!isInstanceOf(field.getGenericType().getClass(), ParameterizedType.class)) {
			return new Class<?>[0];
		}
		Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
		return toClassArray(types);
	}

	public static Class<?> toClass(Type t) {
		if(JkReflection.isInstanceOf(t.getClass(), ParameterizedType.class)) {
			return (Class<?>) (((ParameterizedType) t).getRawType());
		} else {
			return (Class<?>) t;
		}
	}
	public static Class<?>[] toClassArray(Type[] types) {
		return JkStreams.map(Arrays.asList(types), JkReflection::toClass).toArray(new Class<?>[0]);
	}

	public static Class<?> classForName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new JkRuntimeException(e, "Class not found for name: {}", className);
		}
	}
	public static Class<?> classForNameSafe(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * @param fieldsToCopy can be the simple field name, if in both class the name
	 *                     is equals, or in the form 'id=entityId' (source=target)
	 */
	public static <T> T copyFields(Object source, Class<T> targetClass, String... fieldsToCopy) {
		T target = createInstance(targetClass);
		copyFields(source, target, fieldsToCopy);
		return target;
	}
	public static void copyFields(Object source, Object target, String... fieldsToCopy) {
		Map<String, Field> sourceFieldMap = JkStreams.toMapSingle(findAllFields(source.getClass()), Field::getName);
		List<Field> targetFields = findAllFields(target.getClass());

		Map<String, String> fnames;
		if(fieldsToCopy.length > 0) {
			fnames = getFieldNameMap(fieldsToCopy);
		} else {
			fnames = JkStreams.toMapSingle(targetFields, Field::getName, Field::getName);
		}

		JkFormatter fmt = JkFormatter.get();
		for (Field tf : targetFields) {
			String sourceFieldName = fnames.get(tf.getName());
			if(sourceFieldName != null) {
				Field sf = sourceFieldMap.get(sourceFieldName);
				if (sf != null) {
					if (sf.getType() == tf.getType()) {
						Object sval = getFieldValue(source, sf);
						setFieldValue(target, tf, sval);
					} else if (tf.getType() == String.class) {
						String sval = fmt.formatFieldValue(getFieldValue(source, sf), sf);
						setFieldValue(target, tf, sval);
					} else if (sf.getType() == String.class) {
						String sval = getFieldValue(source, sf);
						Object o = fmt.parseFieldValue(sval, tf);
						setFieldValue(target, tf, o);
					} else {
						String sval = fmt.formatFieldValue(getFieldValue(source, sf), sf);
						Object o = fmt.parseFieldValue(sval, tf);
						setFieldValue(target, tf, o);
					}
				}
			}
		}
	}

	// return a map of <target field name, source field name>
	private static Map<String, String> getFieldNameMap(String... fieldNames) {
		Map<String, String> toRet = new LinkedHashMap<>();
		for (String fstr : fieldNames) {
			String trimmed = fstr.replaceAll("\\s+", " ").trim();
			List<String> tlist = JkStrings.splitList(trimmed, " ", true);
			tlist.forEach(t -> {
				if(!t.contains("=")) {
					toRet.put(t, t);
				} else {
					String[] arr = JkStrings.splitArr(t, "=", true);
					toRet.put(arr[1], arr[0]);
				}
			});
		}
		return toRet;
	}

}
