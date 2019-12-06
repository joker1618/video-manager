package xxx.joker.libs.core.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

public class JkDates {

    public static final DateTimeFormatter FMT_AOD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter FMT_AODT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /* PARSER */
    public static LocalDate toDate(String str, String pattern) {
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
    public static LocalTime toTime(String str, String pattern) {
        try {
            return LocalTime.parse(str, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
    public static LocalDateTime toDateTime(String str, String pattern) {
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /* FORMAT */
    public static String format(TemporalAccessor ldt, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(ldt);
    }

    /* CHECKS */
    public static boolean isAOD(String source) {
        try {
            FMT_AOD.parse(source);
            return true;
        } catch(DateTimeParseException ex) {
            return false;
        }
    }
    public static boolean areAODs(List<String> source) {
        for(String elem : source) {
            if(!isAOD(elem)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDate(String source, String format) {
        return isDate(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean isDate(String source, DateTimeFormatter formatter) {
        try {
            formatter.parse(source);
            return true;
        } catch(DateTimeParseException ex) {
            return false;
        }
    }
    public static boolean areDates(List<String> source, String format) {
        return areDates(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean areDates(List<String> source, DateTimeFormatter formatter) {
        for(String elem : source) {
            if(!isDate(elem, formatter)) {
                return false;
            }
        }
        return true;
    }
    public static boolean areDates(String[] sarr, DateTimeFormatter formatter) {
        return areDates(Arrays.asList(sarr), formatter);
    }

    public static boolean isTime(String source, String format) {
        return isTime(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean isTime(String source, DateTimeFormatter formatter) {
        try {
            formatter.parse(source);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    public static boolean areTimes(List<String> source, String format) {
        return areTimes(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean areTimes(List<String> source, DateTimeFormatter formatter) {
        for(String elem : source) {
            if(!isTime(elem, formatter)) {
                return false;
            }
        }
        return true;
    }
    public static boolean areTimes(String[] sarr, DateTimeFormatter formatter) {
        return areTimes(Arrays.asList(sarr), formatter);
    }

    public static boolean isDateTime(String source, String format) {
        return isDateTime(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean isDateTime(String source, DateTimeFormatter formatter) {
        try {
            formatter.parse(source);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    public static boolean areDateTimes(List<String> source, String format) {
        return areDateTimes(source, DateTimeFormatter.ofPattern(format));
    }
    public static boolean areDateTimes(List<String> source, DateTimeFormatter formatter) {
        for(String elem : source) {
            if(!isDateTime(elem, formatter)) {
                return false;
            }
        }
        return true;
    }
    public static boolean areDateTimes(String[] sarr, DateTimeFormatter formatter) {
        return areDateTimes(Arrays.asList(sarr), formatter);
    }

    public static boolean isValidDateTimeFormatter(String format) {
        try {
            DateTimeFormatter.ofPattern(format);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }


}
