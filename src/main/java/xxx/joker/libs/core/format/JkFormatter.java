package xxx.joker.libs.core.format;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.csv.CsvPlaceholder;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkReflection;
import xxx.joker.libs.core.runtime.wrapper.TypeWrapper;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static xxx.joker.libs.core.format.csv.CsvConst.*;
import static xxx.joker.libs.core.lambda.JkStreams.filter;
import static xxx.joker.libs.core.runtime.JkReflection.*;
import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Parse and format object of every class.
 *
 * The parse/format can be customized adding specific functions:
 * - for a field
 * - for a class type
 * - for an instance of a type
 *
 * In format, new columns can be added using 'customFormats'.
 * After parse can be run a Consumer, using 'afterParseConsumers'.
 *
 * After parse or format a collection/csv, any errors are stored in 'errorsFormat' and 'errorsParse'.
 *
 * In format, if a type is not managed, is used the 'toString' method.
 * In parse, if a type is not managed, is used null value.
 *
 * Nested structures allowed (also recursively)
 *
 * SIMPLE TYPES MANAGED:
 * - boolean
 * - short
 * - int
 * - long
 * - float
 * - double
 * 
 * - Boolean		
 * - Short
 * - Integer
 * - Long
 * - Float		    
 * - Double		
 * 
 * - LocalTime
 * - LocalDate
 * - LocalDateTime
 *  
 * - File
 * - Path
 *
 * - Class
 * - Enum
 * - String
 *
 * - JkFormattable
 *
 * STRUCT TYPES MANAGED:
 * - formatted
 *      Pair
 *      Collection
 *      Map
 *  - parsed
 *      Pair
 *
 *      List --> ArrayList
 *      ArrayList
 *      LinkedList
 *
 *      Set --> TreeSet if comparable, else LinkedHashSet
 *      TreeSet
 *      LinkedHashSet
 *      HashSet
 *
 *      Map --> TreeMap if key is comparable, else LinkedHashMap
 *      TreeMap
 *      LinkedHashMap
 *      HashMap
 *
 */

public class JkFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(JkFormatter.class);

    private DateTimeFormatter DTF_TIME = DateTimeFormatter.ISO_LOCAL_TIME;
    private DateTimeFormatter DTF_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private DateTimeFormatter DTF_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);

    private Map<Field, Function<?, String>> fieldFormats = new HashMap<>();
    private Map<Class<?>, Function<?, String>> classFormats = new HashMap<>();
    private Map<Class<?>, Function<?, String>> instanceFormats = new HashMap<>();

    private Map<Field, Function<String, ?>> fieldParses = new HashMap<>();
    private Map<Class<?>, Function<String, ?>> classParses = new HashMap<>();
    private Map<Class<?>, Function<String, ?>> instanceParses = new HashMap<>();

    private Map<String, Function<?, String>> customFormats = new LinkedHashMap<>();
    private List<Consumer<?>> afterParseConsumers = new ArrayList<>();

    // Contains the last errors: not parsed lines and not formatted objects
    private Map<Object, List<String>> errorsFormat = new LinkedHashMap<>();
    private Map<String, List<String>> errorsParse = new LinkedHashMap<>();

    private boolean formatStaticFields = false;

    private static final String PH_LEVEL = "lv";

    private JkFormatter() {
        numberFormat.setGroupingUsed(false);
    }

    public static JkFormatter get() {
        return new JkFormatter();
    }

    public <T> List<T> parseCsv(Path csvPath, Class<T> clazz) {
        return parseCsv(JkFiles.readLines(csvPath), clazz, true);
    }
    public <T> List<T> parseCsv(List<String> csvLines, Class<T> clazz, boolean includeFields, String... fieldNames)  {
        return parseCsv(csvLines, clazz, includeFields, JkConvert.toList(fieldNames));
    }
    public <T> List<T> parseCsv(Path csvPath, Class<T> clazz, boolean includeFields, Collection<String> fieldNames)  {
        return parseCsv(JkFiles.readLines(csvPath), clazz, includeFields, fieldNames);
    }
    public <T> List<T> parseCsv(List<String> csvLines, Class<T> clazz, boolean includeFields, Collection<String> fieldNames)  {
        return parseCsv1(csvLines, SEP_FIELD, clazz, includeFields, fieldNames);
    }
    private <T> List<T> parseCsv1(List<String> lines, CsvPlaceholder sep, Class<T> clazz, boolean includeFields, Collection<String> fieldNames)  {
        List<T> toRet = new ArrayList<>();
        errorsParse.clear();

        List<String> csvLines = filter(lines, StringUtils::isNotBlank);

        if(!csvLines.isEmpty()) {
            List<String> fnames = JkStrings.splitList(csvLines.get(0), sep.getSeparator());
            int numColumnsExpected = fnames.size();
            if(fieldNames.size() > 0) {
                if(includeFields) {
                    fnames.removeIf(f -> !JkTests.containsIgnoreCase(fieldNames, f));
                } else {
                    fnames.removeIf(f -> JkTests.containsIgnoreCase(fieldNames, f));
                }
            }

            Map<String, Field> fmap = JkStreams.toMapSingle(fnames, fn -> fn, f -> getFieldByName(clazz, f));
            for(int i = 1; i < csvLines.size(); i++) {
                String csvLine = csvLines.get(i);
                T elem = JkReflection.createInstance(clazz);
                List<String> line = JkStrings.splitList(csvLine, sep.getSeparator());
                if(line.size() != numColumnsExpected) {
                    LOG.warn("Skipped line: columns found {}, expected {}.  [{}]", line.size(), numColumnsExpected, csvLine);
                } else {
                    for (int col = 0; col < fnames.size(); col++) {
                        String fieldName = fnames.get(col);
                        try {
                            Field f = fmap.get(fieldName);
                            if (f != null) {
                                String strVal = unescapeString(line.get(col), false, sep);
                                if (strVal != null) {
                                    Object value = parseFieldValue(strVal, f);
                                    JkReflection.setFieldValue(elem, f, value);
                                }
                            }
                        } catch (Throwable t) {
                            LOG.error(strf("Error parsing field [{}] for line [{}]", fieldName, csvLine), t);
                            errorsParse.putIfAbsent(csvLine, new ArrayList<>());
                            errorsParse.get(csvLine).add(fieldName);
                        }
                    }
                    for (Consumer consumer : afterParseConsumers) {
                        try {
                            consumer.accept(elem);
                        } catch (Throwable t) {
                            LOG.error(strf("Error in after parse consumer for object [{}]", elem), t);
                            errorsParse.putIfAbsent(csvLine, new ArrayList<>());
                            errorsParse.get(csvLine).add("after parse consumer");
                        }
                    }
                    toRet.add(elem);
                }
            }
        }
        return toRet;
    }
    public Object parseFieldValue(String value, Field field) {
        Object o;

        if(value.equals(PH_NULL)){
            o = null;
        } else {
            TypeWrapper fw = new TypeWrapper(field.getGenericType());
            Function<String, ?> parseFunc = retrieveSpecificParser(field);
            if (parseFunc != null) {
                o = parseFunc.apply(value);
            } else {
                o = parseValue(value, fw);
            }
        }

        return o;
    }
    public Object parseValue(String value, TypeWrapper typeWrapper) {
        Object o = null;

        if(value.equals(PH_NULL) || (value.isEmpty() && !typeWrapper.isOfClass(String.class))){
            o = null;
        } else {
            Class<?> twClazz = typeWrapper.getTypeClass();
            Function<String, ?> parseFunc = retrieveSpecificParser(twClazz);
            if (parseFunc != null) {
                o = parseFunc.apply(value);

            } else if(typeWrapper.isCollection()) {
                TypeWrapper twColl = typeWrapper.getParamTypes().get(0);
                boolean isString = twColl.getTypeClass() == String.class;
                CsvPlaceholder sep = SEP_LIST;
                List<String> elems = JkStrings.splitList(value, sep.getSeparator());
                List<Object> parsedElems = new ArrayList<>();
                elems.forEach(elem -> {
                    String unescaped = unescapeString(elem, isString, sep);
                    Object obj = unescaped == null ? null : parseValue(unescaped, twColl);
                    parsedElems.add(obj);
                });
                if(typeWrapper.isList()) {
                    if(typeWrapper.isOfClass(LinkedList.class)) {
                        o = new LinkedList<>(parsedElems);
                    } else if(typeWrapper.isOfClass(ArrayList.class, List.class)) {
                        o = new ArrayList<>(parsedElems);
                    } else {
                        LOG.warn("The List type '{}' is not managed", twClazz);
                    }
                } else if(typeWrapper.isSet()) {
                    if(typeWrapper.isOfClass(HashSet.class)) {
                        o = new HashSet<>(parsedElems);
                    } else if(typeWrapper.isOfClass(LinkedHashSet.class)) {
                        o = new LinkedHashSet<>(parsedElems);
                    } else if(typeWrapper.isOfClass(TreeSet.class)) {
                        o = new TreeSet<>(parsedElems);
                    } else if(typeWrapper.isOfClass(Set.class)) {
                        o = twColl.isComparable() ? new TreeSet<>(parsedElems) : new LinkedHashSet<>(parsedElems);
                    } else {
                        LOG.warn("The Set type '{}' is not managed", twClazz);
                    }
                } else {
                    LOG.warn("The Collection type '{}' is not managed", twClazz);
                }

            } else if(typeWrapper.isMap()) {
                TypeWrapper twKey = typeWrapper.getParamTypes().get(0);
                TypeWrapper twValue = typeWrapper.getParamTypes().get(1);
                CsvPlaceholder sepEntries = SEP_MAP_ENTRIES;
                CsvPlaceholder sepKeyValue = SEP_KEY_VALUE;
                List<String> entries = JkStrings.splitList(value, sepEntries.getSeparator());
                entries = JkStreams.map(entries, entry -> unescapeString(entry, false, sepEntries));
                Map<Object, Object> parsedEntries = new LinkedHashMap<>();
                entries.forEach(entry -> {
                    String[] split = JkStrings.splitArr(entry, sepKeyValue.getSeparator());
                    String unescKey = unescapeString(split[0], isOfClass(twKey.getTypeClass(), String.class), sepKeyValue);
                    String unescValue = unescapeString(split[1], isOfClass(twValue.getTypeClass(), String.class), sepKeyValue);
                    Object k = unescKey == null ? null : parseValue(unescKey, twKey);
                    Object v = unescValue == null ? null : parseValue(unescValue, twValue);
                    parsedEntries.put(k, v);
                });
                if(typeWrapper.isOfClass(HashMap.class)) {
                    o = new HashMap<>(parsedEntries);
                } else if(typeWrapper.isOfClass(TreeMap.class)) {
                    o = new TreeMap<>(parsedEntries);
                } else if(typeWrapper.isOfClass(LinkedHashMap.class)) {
                    o = parsedEntries;
                } else if(typeWrapper.isOfClass(Map.class)) {
                    o = twKey.isComparable() ? new TreeMap<>(parsedEntries) : new LinkedHashMap<>(parsedEntries);
                } else {
                    LOG.warn("The Map type '{}' is not managed", twClazz);
                }

            } else if(typeWrapper.instanceOf(Pair.class)) {
                TypeWrapper twKey = typeWrapper.getParamTypes().get(0);
                TypeWrapper twValue = typeWrapper.getParamTypes().get(1);
                CsvPlaceholder sepKeyValue = SEP_KEY_VALUE;
                String[] split = JkStrings.splitArr(value, sepKeyValue.getSeparator());
                String unescKey = unescapeString(split[0], isOfClass(twKey.getTypeClass(), String.class), sepKeyValue);
                String unescValue = unescapeString(split[1], isOfClass(twValue.getTypeClass(), String.class), sepKeyValue);
                Object k = unescKey == null ? null : parseValue(unescKey, twKey);
                Object v = unescValue == null ? null : parseValue(unescValue, twValue);
                o = Pair.of(k, v);

            } else {
                o = parseSingleValue(value, twClazz);
            }
        }

        return o;
    }
    private Object parseSingleValue(String value, Class<?> valueClazz) {
        Object o = null;

        try {
            if (!value.equals(PH_NULL)) {
                boolean isString = isOfClass(valueClazz, String.class);
                Function<String, ?> parseFunc = retrieveSpecificParser(valueClazz);
                if (parseFunc != null) {
                    o = parseFunc.apply(value);
                } else if (isOfClass(valueClazz, boolean.class, Boolean.class)) {
                    o = Boolean.valueOf(value);
                } else if (isOfClass(valueClazz, short.class, Short.class)) {
                    o = numberFormat.parse(value).shortValue();
                } else if (isOfClass(valueClazz, int.class, Integer.class)) {
                    o = numberFormat.parse(value).intValue();
                } else if (isOfClass(valueClazz, long.class, Long.class)) {
                    o = numberFormat.parse(value).longValue();
                } else if (isOfClass(valueClazz, float.class, Float.class)) {
                    o = numberFormat.parse(value).floatValue();
                } else if (isOfClass(valueClazz, double.class, Double.class)) {
                    o = numberFormat.parse(value).doubleValue();
                } else if (isOfClass(valueClazz, Path.class)) {
                    o = Paths.get(value);
                } else if (isOfClass(valueClazz, File.class)) {
                    o = new File(value);
                } else if (isOfClass(valueClazz, LocalTime.class)) {
                    o = LocalTime.parse(value);
                } else if (isOfClass(valueClazz, LocalDate.class)) {
                    o = LocalDate.parse(value);
                } else if (isOfClass(valueClazz, LocalDateTime.class)) {
                    o = LocalDateTime.parse(value);
                } else if (isInstanceOf(valueClazz, JkFormattable.class)) {
                    o = JkReflection.createInstance(valueClazz);
                    ((JkFormattable) o).parse(value);
                } else if (isInstanceOf(valueClazz, Enum.class)) {
                    o = Enum.valueOf((Class) valueClazz, value);
                } else if (isOfClass(valueClazz, Class.class)) {
                    o = JkReflection.classForName(value);
                } else if (isString) {
                    o = value;
                }
            }
        } catch (Exception ex) {
            throw new JkRuntimeException(ex);
        }

        return o;
    }
    private Function<String, ?> retrieveSpecificParser(Field field) {
        // Search in field format
        Function<String, ?> func;
        if(field != null) {
            func = fieldParses.get(field);
            if (func != null) {
                return func;
            }
            return retrieveSpecificParser(field.getType());
        }
        return null;
    }
    private Function<String, ?> retrieveSpecificParser(Class<?> clazz) {
        Function<String, ?> func;
        // Search in class format
        func = classParses.get(clazz);
        if(func != null) {
            return func;
        }
        // Search in instance format
        List<Function<String, ?>> functions = JkStreams.filterMap(instanceParses.entrySet(), cc -> isInstanceOf(clazz, cc.getKey()), Map.Entry::getValue);
        if(!functions.isEmpty()) {
            return functions.get(0);
        }
        return null;
    }

    public List<String> formatCsv(Collection<?> list)  {
        return formatCsv(list, true);
    }
    public List<String> formatCsv(Collection<?> list, boolean includeFields, String... fieldNames)  {
        return formatCsv1(list, SEP_FIELD, includeFields, fieldNames);
    }
    private List<String> formatCsv1(Collection<?> coll, CsvPlaceholder fieldSep, boolean includeFields, String[] fieldNames)  {
        List<String> toRet = new ArrayList<>();
        errorsFormat.clear();

        if(!coll.isEmpty()) {
            List<?> list = JkConvert.toList(coll);
            List<Field> allFields = JkReflection.findAllFields(list.get(0).getClass());
            if(fieldNames.length > 0) {
                if(includeFields) {
                    allFields.removeIf(f -> !JkTests.contains(fieldNames, f.getName()));
                } else {
                    allFields.removeIf(f -> JkTests.contains(fieldNames, f.getName()));
                }
            }
            if(!formatStaticFields) {
                allFields.removeIf(f -> Modifier.isStatic(f.getModifiers()));
            }

            String header = JkStreams.join(allFields, fieldSep.getSeparator(), Field::getName);
            if(!customFormats.isEmpty()) {
                header += fieldSep.getSeparator() + JkStreams.join(customFormats.keySet(), fieldSep.getSeparator());
            }
            toRet.add(header);

            for (Object elem : list) {
                StringBuilder sb = new StringBuilder();
                for (Field field : allFields) {
                    if (sb.length() > 0)    sb.append(fieldSep.getSeparator());
                    try {
                        Object val = getFieldValue(elem, field);
                        String fmtValue = formatFieldValue(val, field);
                        sb.append(escapeString(fmtValue, false, fieldSep));
                    } catch (Throwable t) {
                        LOG.error(strf("Error formatting field [{}] for object [{}]", field.getName(), elem), t);
                        errorsFormat.putIfAbsent(elem, new ArrayList<>());
                        errorsFormat.get(elem).add(field.getName());
                        sb.append(PH_NULL);
                    }
                }
                for (Map.Entry<String, Function<?, String>> entry : customFormats.entrySet()) {
                    if (sb.length() > 0)    sb.append(fieldSep.getSeparator());
                    try {
                        Function<Object, String> func = (Function<Object, String>) entry.getValue();
                        String fmtValue = func.apply(elem);
                        sb.append(escapeString(fmtValue, false, fieldSep));
                    } catch (Throwable t) {
                        LOG.error(strf("Error formatting field [{}] for object [{}]", entry.getKey(), elem), t);
                        errorsFormat.putIfAbsent(elem, new ArrayList<>());
                        errorsFormat.get(elem).add(entry.getKey());
                        sb.append(PH_NULL);
                    }
                }
                toRet.add(sb.toString());
            }
        }

        return toRet;
    }
    public <T> String formatFieldValue(T value, Field field) {
        String toRet;

        if (value == null) {
            toRet = PH_NULL;
        } else {
            TypeWrapper fw = new TypeWrapper(field.getGenericType());
            Function<?, String> toStringFmt = retrieveSpecificFormat(field);
            if (toStringFmt != null) {
                Function<T, String> fmtFunc = (Function<T, String>) toStringFmt;
                toRet = fmtFunc.apply(value);
            } else {
                toRet = formatValue(value, fw);
            }
        }

        return toRet;
    }
    public String formatValue(Object value, TypeWrapper typeWrapper) {
        String toRet;

        if (value == null) {
            toRet = PH_NULL;
        } else {
            Class<?> valueClazz = typeWrapper.getTypeClass();
            Function<?, String> toStringFmt = retrieveSpecificFormat(valueClazz);
            if (toStringFmt != null) {
                Function<Object, String> fmtFunc = (Function<Object, String>) toStringFmt;
                toRet = fmtFunc.apply(value);

            } else if(typeWrapper.isCollection()) {
                TypeWrapper tw = typeWrapper.getParamTypes().get(0);
                boolean isString = tw.getTypeClass() == String.class;
                Collection<?> coll = (Collection) value;
                CsvPlaceholder csvPh = SEP_LIST;
                List<String> parts = new ArrayList<>();
                coll.forEach(elem -> {
                    String s = formatValue(elem, tw);
                    parts.add(escapeString(s, isString, csvPh));
                });
                toRet = JkStreams.join(parts, csvPh.getSeparator());

            } else if(typeWrapper.isMap()) {
                TypeWrapper twKey = typeWrapper.getParamTypes().get(0);
                TypeWrapper twValue = typeWrapper.getParamTypes().get(1);
                Map<?,?> map = (Map) value;
                List<String> parts = new ArrayList<>();
                CsvPlaceholder sepKeyValue = SEP_KEY_VALUE;
                CsvPlaceholder sepEntries = SEP_MAP_ENTRIES;
                map.forEach((k,v) -> {
                    String strKey = escapeString(formatValue(k, twKey), isOfClass(twKey.getTypeClass(), String.class), sepKeyValue);
                    String strValue = escapeString(formatValue(v, twValue), isOfClass(twValue.getTypeClass(), String.class), sepKeyValue);
                    parts.add(escapeString(strKey + sepKeyValue.getSeparator() + strValue, false, sepEntries));
                });
                toRet = JkStreams.join(parts, sepEntries.getSeparator());

            } else if(typeWrapper.instanceOf(Pair.class)) {
                TypeWrapper twKey = typeWrapper.getParamTypes().get(0);
                TypeWrapper twValue = typeWrapper.getParamTypes().get(1);
                Pair<?,?> pair = (Pair) value;
                CsvPlaceholder sepKeyValue = SEP_KEY_VALUE;
                String strKey = escapeString(formatValue(pair.getKey(), twKey), isOfClass(twKey.getTypeClass(), String.class), sepKeyValue);
                String strValue = escapeString(formatValue(pair.getValue(), twValue), isOfClass(twValue.getTypeClass(), String.class), sepKeyValue);
                toRet = strKey + sepKeyValue.getSeparator() + strValue;

            } else {
                toRet = formatSingleValue(value, valueClazz);
            }
        }

        return toRet;
    }
    private String formatSingleValue(Object value, Class<?> valueClazz) {
        String toRet;

        if (value == null) {
            toRet = PH_NULL;
        } else {
            boolean isString = isOfClass(valueClazz, String.class);
            Function<?, String> toStringFmt = retrieveSpecificFormat(valueClazz);
            if (toStringFmt != null) {
                Function<Object, String> fmtFunc = (Function<Object, String>) toStringFmt;
                toRet = fmtFunc.apply(value);
            } else if (isOfClass(valueClazz, boolean.class, Boolean.class)) {
                toRet = ((Boolean) value).toString();
            } else if (isOfClass(valueClazz, File.class, Path.class)) {
                toRet = value.toString();
            } else if (isOfClass(valueClazz, LocalTime.class)) {
                toRet = DTF_TIME.format((LocalTime) value);
            } else if (isOfClass(valueClazz, LocalDate.class)) {
                toRet = DTF_DATE.format((LocalDate) value);
            } else if (isOfClass(valueClazz, LocalDateTime.class)) {
                toRet = DTF_DATETIME.format((LocalDateTime) value);
            } else if (isOfClass(valueClazz, short.class, Short.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class, double.class, Double.class)) {
                toRet = numberFormat.format(value);
            } else if (isInstanceOf(valueClazz, JkFormattable.class)) {
                toRet = ((JkFormattable) value).format();
            } else if (isInstanceOf(valueClazz, Enum.class)) {
                toRet = ((Enum) value).name();
            } else if (isOfClass(valueClazz, Class.class)) {
                toRet = ((Class)value).getName();
            } else if (isString){
                toRet = (String) value;
            } else {
                toRet = value.toString();
            }
        }

        return toRet;
    }
    private Function<?, String> retrieveSpecificFormat(Field field) {
        // Search in field format
        Function<?, String> func;
        if(field != null) {
            func = fieldFormats.get(field);
            if (func != null) {
                return func;
            }
            return retrieveSpecificFormat(field.getType());
        }
        return null;
    }
    private Function<?, String> retrieveSpecificFormat(Class<?> clazz) {
        Function<?, String> func;
        // Search in class format
        func = classFormats.get(clazz);
        if(func != null) {
            return func;
        }
        // Search in instance format
        List<Function<?, String>> functions = JkStreams.filterMap(instanceFormats.entrySet(), cc -> isInstanceOf(clazz, cc.getKey()), Map.Entry::getValue);
        if(!functions.isEmpty()) {
            return functions.get(0);
        }
        return null;
    }

    private String escapeString(String value, boolean fullEscape, CsvPlaceholder csvPh) {
        if(value == null) {
            return PH_NULL;
        }

        String res = changePlaceholderLevels(value, 1, csvPh);
        res = res.replace(csvPh.getSeparator(), PH_LEVEL + "0" + csvPh.getPlaceholder());

        if(fullEscape) {
            res = res.replaceAll(TAB.getSeparator(), TAB.getPlaceholder());
            res = res.replaceAll(NEWLINE.getSeparator(), NEWLINE.getPlaceholder());
        }

        return res;
    }
    private String unescapeString(String value, boolean fullEscape, CsvPlaceholder csvPh) {
        if(PH_NULL.equals(value)) {
            return null;
        }

        String res = value.replace(PH_LEVEL + "0" + csvPh.getPlaceholder(), csvPh.getSeparator());
        res = changePlaceholderLevels(res, -1, csvPh);

        if(fullEscape) {
            res = res.replaceAll(TAB.getPlaceholder(), TAB.getSeparator());
            res = res.replaceAll(NEWLINE.getPlaceholder(), NEWLINE.getSeparator());
        }

        return res;
    }
    private String changePlaceholderLevels(String str, int add, CsvPlaceholder csvPh) {
        List<String> parts = JkStrings.splitList(str, csvPh.getPlaceholder());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if(i == parts.size() - 1) {
                sb.append(part);
            } else {
                int idx = part.lastIndexOf(PH_LEVEL) + PH_LEVEL.length();
                int level = JkConvert.toInt(part.substring(idx)) + add;
                sb.append(part.substring(0, idx) + level + csvPh.getPlaceholder());
            }
        }
        return sb.toString();
    }

    public <T> void setFieldFormat(Class<T> clazz, String fieldName, Function<T, String> formatFunc) {
        fieldFormats.put(getFieldByName(clazz, fieldName), formatFunc);
    }
    public <T> void setFieldFormat(Field field, Function<T, String> formatFunc) {
        fieldFormats.put(field, formatFunc);
    }
    public <T> void setClassFormat(Class<T> clazz, Function<T, String> formatFunc) {
        classFormats.put(clazz, formatFunc);
    }
    public <T> void setInstanceFormat(Class<T> clazz, Function<T, String> formatFunc) {
        instanceFormats.put(clazz, formatFunc);
    }

    public <T> void setFieldParse(Class<T> clazz, String fieldName, Function<String, T> parseFunc) {
        fieldParses.put(getFieldByName(clazz, fieldName), parseFunc);
    }
    public <T> void setFieldParse(Field field, Function<String, T> parseFunc) {
        fieldParses.put(field, parseFunc);
    }
    public <T> void setClassParse(Class<T> clazz, Function<String, T> parseFunc) {
        classParses.put(clazz, parseFunc);
    }
    public <T> void setInstanceParse(Class<T> clazz, Function<String, T> parseFunc) {
        instanceParses.put(clazz, parseFunc);
    }

    public <T> void setCustomFormat(String headerName, Function<T, String> formatFunc) {
        customFormats.put(headerName, formatFunc);
    }
    public <T> void addAfterParseConsumer(Consumer<T> afterParseConsumer) {
        afterParseConsumers.add(afterParseConsumer);
    }

    public <T> Map<T, List<String>> getErrorsFormat() {
        return (Map<T, List<String>>) errorsFormat;
    }
    public Map<String, List<String>> getErrorsParse() {
        return errorsParse;
    }

    public void setFormatStaticFields(boolean formatStaticFields) {
        this.formatStaticFields = formatStaticFields;
    }
}
