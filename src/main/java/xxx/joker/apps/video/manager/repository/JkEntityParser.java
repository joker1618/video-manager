package xxx.joker.apps.video.manager.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.repository.entity.JkEntity;
import xxx.joker.apps.video.manager.repository.entity.JkEntityField;
import xxx.joker.libs.javalibs.exception.JkRuntimeException;
import xxx.joker.libs.javalibs.utils.JkReflection;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

class JkEntityParser {

    private static final Logger logger = LoggerFactory.getLogger(JkEntityParser.class);

    private static final String DATA_FIELD_SEP = "###FIELD_SEP###";
    private static final String DATA_LIST_SEP = "###LIST_SEP###";
    private static final String PH_TAB = "##TAB##";
    private static final String PH_NEWLINE = "##NEWLINE##";

    private Map<Class<?>, Map<Integer, AnnField>> entityFields;

    public JkEntityParser(String pkgToScan) {
        this.entityFields = parseEntityClasses(pkgToScan);
    }

    private Map<Class<?>, Map<Integer, AnnField>> parseEntityClasses(String pkgToScan) {
        Map<Class<?>, Map<Integer, AnnField>> parsedEntities = new HashMap<>();

        logger.info("Scanning package {}", pkgToScan);
        List<Class<?>> entityClasses = retrieveEntityClasses(pkgToScan);

        if(entityClasses.isEmpty()) {
            throw new JkRuntimeException("No JkEntity class found in package {}", pkgToScan);
        }

        logger.info("{} JkEntity class found in package {}", entityClasses.size(), pkgToScan);
        entityClasses.forEach(c -> logger.info("Entity class: {}", c.getName()));

        for(Class<?> clazz : entityClasses) {
            List<Class<?>> dups = JkStreams.filter(parsedEntities.keySet(), c -> c.getSimpleName().equals(clazz.getSimpleName()));
            if(!dups.isEmpty()) {
                // dups has one element only
                throw new IllegalArgumentException(strf("All entity classes must have different class name. Duplicates: {}, {}", clazz.getName(), dups.get(0).getName()));
            }

            if (!parsedEntities.containsKey(clazz)) {
                logger.debug("Entity class {}", clazz.getName());

                Map<Integer, AnnField> annMap = new HashMap<>();
                parsedEntities.put(clazz, annMap);

                List<Field> fields = JkReflection.getFieldsByAnnotation(clazz, JkEntityField.class);

                if (!fields.isEmpty()) {
                    List<AnnField> annFields = JkStreams.map(fields, AnnField::new);

                    for (AnnField annField : annFields) {
                        if (annField.getIndex() < 0) {
                            throw new IllegalArgumentException(strf("Negative index not allowed. Field %s", annField.getField().getName()));
                        }
                        if (annMap.containsKey(annField.getIndex())) {
                            throw new IllegalArgumentException(strf("Duplicated index %d", annField.getIndex()));
                        }
                        if (!annField.isCollection()) {
                            if(annField.getCollectionType() != Object.class) {
                                throw new IllegalArgumentException(strf("Collection type must be specified only for List, Set and arrays (index=%d)", annField.getIndex()));
                            }
                        } else {
                            if(annField.getCollectionType() == Object.class) {
                                throw new IllegalArgumentException(strf("Collection type not specified (index=%d)", annField.getIndex()));
                            }
                        }
                        Class<?> fctype = annField.isCollection() ? annField.getCollectionType() : annField.getFieldType();
                        if(!RepoUtil.isClassAllowed(fctype)) {
                            throw new IllegalArgumentException(strf("Field class %s not allowed", fctype.getName()));
                        }
                        annMap.put(annField.getIndex(), annField);
                    }
                }
            }
        }

        return parsedEntities;
    }

    private List<Class<?>> retrieveEntityClasses(String pkgToScan) {
        List<Class<?>> classes = JkReflection.findClasses(pkgToScan);
        classes.removeIf(c -> !JkReflection.isOfType(c, JkEntity.class));
        return classes;
    }


    private class AnnField {
        private JkEntityField annot;
        private Field field;

        AnnField(Field field) {
            this.annot = field.getAnnotation(JkEntityField.class);
            this.field = field;
        }

        int getIndex() {
            return annot.index();
        }

        public Field getField() {
            return field;
        }

        Class<?> getFieldType() {
            return field.getType();
        }

        Class<?> getParentClass() {
            return field.getDeclaringClass();
        }

        Class<?> getCollectionType() {
            return annot.collectionType();
        }

        boolean isEntityImpl() {
            Class<?> fclazz = isCollection() ? getCollectionType() : getFieldType();
            return JkReflection.isOfType(fclazz, JkEntity.class);
        }

        Class<?> getEntityClass() {
            Class<?> fclazz = isCollection() ? getCollectionType() : getFieldType();
            return !isEntityImpl() ? null : fclazz;
        }

        boolean isCollection() {
            return isList() || isSet();
        }

        boolean isList() {
            return getFieldType() == List.class;
        }

        boolean isSet() {
            return getFieldType() == Set.class;
        }

        boolean isComparable() {
            Class<?> fclazz = isCollection() ? getCollectionType() : getFieldType();
            return JkReflection.isOfType(fclazz, Comparable.class);
        }

        Object getValue(Object elem) throws IllegalAccessException {
            boolean facc = field.isAccessible();
            field.setAccessible(true);
            Object obj = field.get(elem);
            field.setAccessible(facc);
            return obj;
        }

        void setValue(Object elem, Object value) throws IllegalAccessException {
            boolean facc = field.isAccessible();
            field.setAccessible(true);
            field.set(elem, value);
            field.setAccessible(facc);
        }
    }
}
