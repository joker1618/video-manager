package xxx.joker.libs.repo.config;

import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.format.JkFormattable;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkReflection;
import xxx.joker.libs.core.runtime.wrapper.TypeWrapper;
import xxx.joker.libs.repo.design.RepoEntity;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static xxx.joker.libs.core.util.JkConvert.toList;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class RepoConfig {

    public static final String PACKAGE_COMMON_ENTITIES = "xxx.joker.libs.repo.design.entities";

    public static final String REF_TYPE_FIELD_RESOURCE_PATH = "resourcePath";

    public static final String PK_SEP = "-";

    public static final String FOLDER_RESOURCES = "resources";
    public static final String FOLDER_DB = "db";
    public static final String FOLDER_SOURCE_JARS = "sourceJars";

    public static final String PROP_DB_SEQUENCE = "_config.sequence.id.value";

    public static final String KEYWORD_JKREPO = "jkRepo";
    public static final String FORMAT_FKEYS_FILENAME = "{}." + KEYWORD_JKREPO + ".fkeys";

    public static final List<Class<?>> VALID_TYPE_ENTITY_ID = toList(Long.class);
    public static final List<Class<?>> VALID_TYPE_CREATION_TM = toList(JkDateTime.class);
    public static final List<Class<?>> VALID_TYPE_RESOURCE_PATH = toList(Path.class);
    public static final List<Class<?>> VALID_TYPE_FOREIGN_ID = toList(Long.class);

    public static boolean isValidRepoClass(Class<?> clazz) {
        return JkReflection.isInstanceOf(clazz, RepoEntity.class) &&
                !Modifier.isAbstract(clazz.getModifiers()) &&
                !Modifier.isInterface(clazz.getModifiers());
    }
    public static boolean isValidFieldType(TypeWrapper tw) {
        boolean restricted = tw.containsGenericType(RepoEntity.class);

        if(tw.isOfClass(SIMPLE_CLASS_TYPES) ||  tw.instanceOf(SIMPLE_INSTANCE_TYPES))
            return true;

        if(tw.isOfClass(STRUCT_CLASS_TYPES) || tw.isOfClass(STRUCT_INSTANCE_TYPES)) {
            List<TypeWrapper> parTypes = tw.getParamTypes();
            if(tw.isList() || tw.isSet()) {
                if(parTypes.size() != 1) return false;
                TypeWrapper twColl = parTypes.get(0);
                return restricted ? twColl.instanceOf(RepoEntity.class) : isValidFieldType(twColl);

            } else if(tw.isMap()) {
                if (parTypes.size() != 2) return false;
                TypeWrapper twKey = parTypes.get(0);
                TypeWrapper twValue = parTypes.get(1);
                if (restricted) {
                    if (!(twKey.isOfClass(SIMPLE_CLASS_TYPES) || twKey.instanceOf(SIMPLE_INSTANCE_TYPES)))
                        return false;
                    if (twValue.isOfClass(SIMPLE_CLASS_TYPES) || twValue.instanceOf(SIMPLE_INSTANCE_TYPES))
                        return true;
                    if (!twValue.isList() && !twValue.isSet())
                        return false;
                    if (twValue.getParamTypes().size() != 1)
                        return false;
                    TypeWrapper twValueCollElem = twValue.getParamTypes().get(0);
                    return twValueCollElem.isOfClass(SIMPLE_CLASS_TYPES) || twValueCollElem.instanceOf(SIMPLE_INSTANCE_TYPES);

                } else {
                    return isValidFieldType(twKey) && isValidFieldType(twValue);
                }
            }
        }

        return false;
    }
    public static boolean isValidTypeForEntityPK(TypeWrapper tw) {
        return tw.isOfClass(SIMPLE_CLASS_TYPES) || tw.instanceOf(SIMPLE_INSTANCE_TYPES);
    }
    public static boolean isValidTypeForCascadeDelete(TypeWrapper tw) {
        return tw.instanceOf(RepoEntity.class) || (tw.isCollection() && tw.getParamType(0).instanceOf(RepoEntity.class));
    }

    //region FIELD TYPES
    public static final List<Class<?>> SIMPLE_INSTANCE_TYPES = Arrays.asList(
            RepoEntity.class,
            JkFormattable.class,
            Enum.class
    );
    public static final List<Class<?>> SIMPLE_CLASS_TYPES = Arrays.asList(
            Boolean.class,		boolean.class,
            Short.class,		short.class,
            Integer.class,		int.class,
            Long.class,			long.class,
            Float.class,		float.class,
            Double.class,		double.class,

            LocalTime.class,
            LocalDate.class,
            LocalDateTime.class,

            File.class,
            Path.class,

            String.class,
            Class.class
    );
    public static final List<Class<?>> STRUCT_INSTANCE_TYPES = Arrays.asList(
            List.class,
            Set.class,
            Map.class
    );
    public static final List<Class<?>> STRUCT_CLASS_TYPES = toList(
            ArrayList.class,
            LinkedList.class,

            HashSet.class,
            TreeSet.class,
            LinkedHashSet.class,

            HashMap.class,
            TreeMap.class,
            LinkedHashMap.class
    );
    //endregion

    public static String getDbEntityFileName(String dbName, String clazzSimpleName) {
        return strf("{}.{}.{}.data", dbName, KEYWORD_JKREPO, clazzSimpleName);
    }
    public static String getDbFkeysFileName(String dbName) {
        return strf("{}.{}.fkeys", dbName, KEYWORD_JKREPO);
    }

}
