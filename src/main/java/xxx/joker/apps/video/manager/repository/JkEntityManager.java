package xxx.joker.apps.video.manager.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.repository.entity.*;
import xxx.joker.libs.javalibs.exception.JkRuntimeException;
import xxx.joker.libs.javalibs.utils.JkConverter;
import xxx.joker.libs.javalibs.utils.JkReflection;
import xxx.joker.libs.javalibs.utils.JkStreams;
import xxx.joker.libs.javalibs.utils.JkStrings;

import static xxx.joker.apps.video.manager.repository.JkPersistenceManager.EntityLines;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

class JkEntityManager {

    private static final Logger logger = LoggerFactory.getLogger(JkEntityManager.class);

    private static final String DATA_FIELD_SEP = "###FIELD_SEP###";
    private static final String DATA_LIST_SEP = "###LIST_SEP###";
    private static final String PH_TAB = "##TAB##";
    private static final String PH_NEWLINE = "##NEWLINE##";
    
    private Map<Class<?>, Map<Integer, AnnField>> entityFields;

    public JkEntityManager(String pkgToScan) {
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
    
    public Set<Class<?>> getEntityClasses() {
        return entityFields.keySet();
    }
    
    public Map<Class<?>, TreeSet<JkEntity>> parseData(Map<Class<?>, EntityLines> dataMap) {
        Map<Class<?>, Map<String, JkEntity>> entityMap = new HashMap<>();

        // Parse entities
        for(Class<?> clazz : dataMap.keySet()) {
            List<JkEntity> elist = JkStreams.map(dataMap.get(clazz).getEntityLines(), l -> parseLine(clazz, l));
            entityMap.put(clazz, JkStreams.toMapSingle(elist, JkEntity::getPrimaryKey));
        }

        // Resolve dependencies and fill entities objects
        try {
            for (Class<?> fromClazz : dataMap.keySet()) {
                List<ForeignKey> fkOfClass = JkStreams.map(dataMap.get(fromClazz).getForeignKeyLines(), ForeignKey::new);
                Map<Long, List<ForeignKey>> fromPKmap = JkStreams.toMap(fkOfClass, ForeignKey::getFromID);
                for (Long fromPK : fromPKmap.keySet()) {
                    JkEntity fromObj = entityMap.get(fromClazz).get(fromPK);
                    Map<Integer, List<ForeignKey>> fkIndexMap = JkStreams.toMap(fromPKmap.get(fromPK), ForeignKey::getFromFieldIndex);
                    for (int index : fkIndexMap.keySet()) {
                        List<ForeignKey> fklist = fkIndexMap.get(index);
                        List<JkEntity> elist = JkStreams.mapAndFilter(fklist, fk -> entityMap.get(fk.getDepClazz()).get(fk.getDepID()), Objects::nonNull);
                        if(!elist.isEmpty()) {
                            AnnField annField = entityFields.get(fromClazz).get(index);
                            Object objValue = annField.isCollection() ? listToSafeObject(elist, annField) : elist.get(0);
                            annField.setValue(fromObj, objValue);
                        }
                    }
                }
            }

            Map<Class<?>, TreeSet<JkEntity>> toRet = new HashMap<>();
            for(Class<?> c : entityMap.keySet()) {
                toRet.put(c, JkConverter.toTreeSet(entityMap.get(c).values()));
            }

            return toRet;

        } catch(IllegalAccessException ex) {
            throw new JkRuntimeException(ex, "Error parsing data");
        }
    }

    public Map<Class<?>, EntityLines> formatData(Map<Class<?>, TreeSet<JkEntity>> dataMap) {
        Map<Class<?>, EntityLines> toRet = new HashMap<>();
        for (Class<?> clazz : dataMap.keySet()) {
            EntityLines el = formatEntityClass(clazz, dataMap.get(clazz));
            toRet.put(clazz, el);
        }
        return toRet;
    }

    public Map<Class<?>, Set<JkEntity>> getDependencies(JkEntity obj) {
        try {
            List<AnnField> depAnnFields = JkStreams.filter(entityFields.get(obj.getClass()).values(), AnnField::isEntityImpl);
            Map<Class<?>, Set<JkEntity>> toRet = new HashMap<>();
            for (AnnField annField : depAnnFields) {
                toRet.putIfAbsent(annField.getEntityClass(), new TreeSet<>());
                Object value = annField.getValue(obj);
                if(value != null) {
                    if (annField.isCollection()) {
                        toRet.get(annField.getEntityClass()).addAll(JkStreams.map((Collection) value, v -> (JkEntity) v));
                    } else {
                        toRet.get(annField.getEntityClass()).add((JkEntity) value);
                    }
                }
            }
            return toRet;

        } catch(IllegalAccessException ex) {
            throw new JkRuntimeException(ex);
        }
    }

    private JkEntity parseLine(Class<?> elemClazz, String line) {
        try {
            JkEntity instance = (JkEntity) elemClazz.newInstance();
            List<String> row = JkStrings.splitFieldsList(line, DATA_FIELD_SEP);

            String entityID = row.remove(0);
            instance.setEntityID(JkConverter.stringToLong(entityID));
            String insTstamp = row.remove(0);
            instance.setInsertTstamp(LocalDateTime.parse(insTstamp, DateTimeFormatter.ISO_DATE_TIME));

            for (Map.Entry<Integer, AnnField> entry : entityFields.get(elemClazz).entrySet()) {
                if (entry.getKey() < row.size()) {
                    Object o = fromStringValue(row.get(entry.getKey()), entry.getValue());
                    entry.getValue().setValue(instance, o);
                }
            }

            return instance;

        } catch(ReflectiveOperationException ex) {
            throw new JkRuntimeException(ex);
        }
    }

    private Object fromStringValue(String value, AnnField annField) {
        Object retVal;

        Class<?> fclazz = annField.getFieldType();
        if(annField.isCollection()) {
            List<String> strElems = JkStrings.splitFieldsList(value, DATA_LIST_SEP);
            Class<?> elemClazz = annField.getCollectionType();

            List<Object> values = new ArrayList<>();
            if(!JkReflection.isOfType(elemClazz, JkEntity.class)) {
                values.addAll(JkStreams.map(strElems, elem -> fromStringSingleValue(elem, elemClazz)));
            }

            retVal = listToSafeObject(values, annField);

        } else if(JkReflection.isOfType(fclazz, JkEntity.class)) {
            retVal = null;

        } else {
            retVal = fromStringSingleValue(value, fclazz);
        }

        return retVal;
    }

    private Object listToSafeObject(List<?> values, AnnField annField) {
        if(annField.isSet()) {
            return annField.isComparable() ? JkConverter.toTreeSet(values) : JkConverter.toHashSet(values);
        }

        return values;
    }

    private Object fromStringSingleValue(String value, Class<?> fclazz) {
        Object o;

        if (StringUtils.isEmpty(value)) {
            o = fclazz == String.class ? "" : null;
        } else if (Arrays.asList(boolean.class, Boolean.class).contains(fclazz)) {
            o = Boolean.valueOf(value);
        } else if (Arrays.asList(boolean.class, Boolean.class).contains(fclazz)) {
            o = Boolean.valueOf(value);
        } else if (Arrays.asList(int.class, Integer.class).contains(fclazz)) {
            o = JkConverter.stringToInteger(value);
        } else if (Arrays.asList(int.class, Integer.class).contains(fclazz)) {
            o = JkConverter.stringToInteger(value);
        } else if (Arrays.asList(long.class, Long.class).contains(fclazz)) {
            o = JkConverter.stringToLong(value);
        } else if (Arrays.asList(long.class, Long.class).contains(fclazz)) {
            o = JkConverter.stringToLong(value);
        } else if (Arrays.asList(double.class, Double.class).contains(fclazz)) {
            o = JkConverter.stringToDouble(value);
        } else if (Arrays.asList(double.class, Double.class).contains(fclazz)) {
            o = JkConverter.stringToDouble(value);
        } else if (Arrays.asList(float.class, Float.class).contains(fclazz)) {
            o = JkConverter.stringToFloat(value);
        } else if (Arrays.asList(float.class, Float.class).contains(fclazz)) {
            o = JkConverter.stringToFloat(value);
        } else if (fclazz == Path.class) {
            o = Paths.get(value);
        } else if (fclazz == File.class) {
            o = new File(value);
        } else if (fclazz == LocalTime.class) {
            o = LocalTime.parse(value, DateTimeFormatter.ISO_TIME);
        } else if (fclazz == LocalDate.class) {
            o = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
        } else if (fclazz == LocalDateTime.class) {
            o = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } else {
            o = value.replaceAll(PH_TAB, "\t").replaceAll(PH_NEWLINE, "\n");
        }

        return o;
    }

    private EntityLines formatEntityClass(Class<?> clazz, TreeSet<JkEntity> dataSet) {
        try {
            EntityLines toRet = new EntityLines(clazz);
            Map<Integer, AnnField> clazzFields = entityFields.get(clazz);
            int numFields = clazzFields.keySet().stream().mapToInt(i->i).max().orElse(-1) + 1;

            for(JkEntity elem : dataSet) {
                Long elemID = retrieveID(elem);
                List<String> row = Stream.generate(() -> "").limit(numFields).collect(Collectors.toList());
                for (Integer index : clazzFields.keySet()) {
                    AnnField annField = clazzFields.get(index);
                    Object value = annField.getValue(elem);
                    Pair<String, Set<Long>> formattedPair = formatValue(value, annField);
                    row.set(index, formattedPair.getKey());
                    Set<Long> depIDs = formattedPair.getValue();
                    if(!depIDs.isEmpty()) {
                        Class<?> fkClazz = annField.isCollection() ? annField.getCollectionType() : annField.getFieldType();
                        List<String> fkLines = depIDs.stream()
                                .sorted()
                                .map(fkID -> new ForeignKey(clazz, elemID, index, fkClazz, fkID))
                                .map(ForeignKey::toRepoLine)
                                .collect(Collectors.toList());
                        toRet.getForeignKeyLines().addAll(fkLines);
                    }
                }


                String entityLine = strf("%s%s%s%s%s", elemID, DATA_FIELD_SEP, elem.getInsertTstamp(), DATA_FIELD_SEP, JkStreams.join(row, DATA_FIELD_SEP));
                toRet.getEntityLines().add(entityLine);
            }

            return toRet;

        } catch (Exception e) {
            throw new JkRuntimeException(e);
        }
    }

    private Long retrieveID(Object o) {
        JkEntity entity = (JkEntity) o;
        Long entityID = entity.getEntityID();
        if(entityID == null) {
            entityID = nextEntityID();
            entity.setEntityID(entityID);
            entity.setInsertTstamp(LocalDateTime.now());
        }
        return entityID;
    }

    private long nextEntityID() {
        // TODO impl
        return 0;
    }

    // return <toStringOfEntity, Set<foreignKeys(ID)>> (key or value)
    private Pair<String, Set<Long>> formatValue(Object value, AnnField annField) {
        Class<?> fclazz = annField.getFieldType();

        String strValue = "";
        Set<Long> foreignKeys = new HashSet<>();

        if(value != null) {
            if (annField.isCollection()) {
                Class<?> elemClazz = annField.getCollectionType();
                List<?> list = annField.isSet() ? JkConverter.toArrayList((Set<?>) value) : (List<?>) value;
                if (!list.isEmpty()) {
                    if (JkReflection.isOfType(elemClazz, JkEntity.class)) {
                        List<Long> fkList = JkStreams.map(list, this::retrieveID);
                        foreignKeys.addAll(fkList);
                    } else {
                        strValue = JkStreams.join(list, DATA_LIST_SEP, e -> toStringSingleValue(e, elemClazz));
                    }
                }

            } else {
                if (JkReflection.isOfType(fclazz, JkEntity.class)) {
                    foreignKeys.add(retrieveID(value));
                } else {
                    strValue = toStringSingleValue(value, fclazz);
                }
            }
        }

        return Pair.of(strValue, foreignKeys);
    }

    private static String toStringSingleValue(Object value, Class<?> fclazz) {
        try {
            String toRet = "";

            if (value == null) {
                toRet = "";
            } else if (Arrays.asList(boolean.class, Boolean.class).contains(fclazz)) {
                toRet = ((Boolean) value) ? "true" : "false";
            } else if (Arrays.asList(File.class, Path.class).contains(fclazz)) {
                toRet = value.toString();
            } else if (fclazz == LocalTime.class) {
                toRet = DateTimeFormatter.ISO_TIME.format((LocalTime) value);
            } else if (fclazz == LocalDate.class) {
                toRet = DateTimeFormatter.ISO_DATE.format((LocalDate) value);
            } else if (fclazz == LocalDateTime.class) {
                toRet = DateTimeFormatter.ISO_DATE_TIME.format((LocalDateTime) value);
            } else if (!JkReflection.isOfType(fclazz, JkEntity.class)) {
                toRet = String.valueOf(value).replaceAll("\t", PH_TAB).replaceAll("\n", PH_NEWLINE);
            }

            return toRet;

        } catch (Exception ex) {
            throw new JkRuntimeException(ex);
        }
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

    private class ForeignKey {
        private Class<?> fromClazz;
        private long fromID;
        private int fromFieldIndex;
        private Class<?> depClazz;
        private long depID;

        public ForeignKey(Class<?> fromClazz, long fromID, int fromFieldIndex, Class<?> depClazz, long depID) {
            this.fromClazz = fromClazz;
            this.fromID = fromID;
            this.fromFieldIndex = fromFieldIndex;
            this.depClazz = depClazz;
            this.depID = depID;
        }

        public ForeignKey(String repoLine) {
            try {
                String[] arr = JkStrings.splitAllFields(repoLine, DATA_FIELD_SEP);
                fromClazz = Class.forName(arr[0]);
                fromID = JkConverter.stringToLong(arr[1]);
                fromFieldIndex = JkConverter.stringToInteger(arr[2]);
                depClazz = Class.forName(arr[3]);
                depID = JkConverter.stringToLong(arr[4]);
            } catch (Exception e) {
                throw new JkRuntimeException(e);
            }
        }

        public String toRepoLine() {
            return fromClazz.getName()
                    + DATA_FIELD_SEP
                    + fromID
                    + DATA_FIELD_SEP
                    + fromFieldIndex
                    + DATA_FIELD_SEP
                    + depClazz.getName()
                    + DATA_FIELD_SEP
                    + depID;
        }

        public Class<?> getFromClazz() {
            return fromClazz;
        }

        public void setFromClazz(Class<?> fromClazz) {
            this.fromClazz = fromClazz;
        }

        public long getFromID() {
            return fromID;
        }

        public void setFromID(long fromID) {
            this.fromID = fromID;
        }

        public int getFromFieldIndex() {
            return fromFieldIndex;
        }

        public void setFromFieldIndex(int fromFieldIndex) {
            this.fromFieldIndex = fromFieldIndex;
        }

        public Class<?> getDepClazz() {
            return depClazz;
        }

        public void setDepClazz(Class<?> depClazz) {
            this.depClazz = depClazz;
        }

        public long getDepID() {
            return depID;
        }

        public void setDepID(long depID) {
            this.depID = depID;
        }
    }


    
}
