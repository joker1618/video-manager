package xxx.joker.libs.core.datetime;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.format.JkSortFormattable;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class JkDateTime extends JkSortFormattable<JkDateTime> {

    private static final DateTimeFormatter DEF_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private LocalDateTime ldt;

    public JkDateTime() {

    }
    private JkDateTime(LocalDateTime ldt) {
        init(ldt);
    }

    public static JkDateTime of(long millis) {
        return of(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime());


    }
    public static JkDateTime of(LocalDateTime ldt) {
        return new JkDateTime(ldt);
    }
    public static JkDateTime of(LocalDate ld) {
        return new JkDateTime(LocalDateTime.of(ld, LocalTime.of(0, 0, 0)));
    }

    public static JkDateTime now() {
        return new JkDateTime(LocalDateTime.now());
    }

    public long totalMillis() {
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public LocalDateTime getDateTime() {
        return ldt;
    }

    public void setDateTime(LocalDateTime ldt) {
        this.ldt = ldt;
    }

    @Override
    public int compareTo(JkDateTime o) {
        return ldt.compareTo(o.ldt);
    }
    public int compareTo(LocalDateTime oldt) {
        return ldt.compareTo(oldt);
    }

    @Override
    public String format() {
        String format = ldt.format(DEF_FMT);
        return StringUtils.rightPad(format, 23, "0");
    }

    public String toAod() {
        return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
    public String format(String pattern) {
        return ldt.format(DateTimeFormatter.ofPattern(pattern));
    }
    public String format(DateTimeFormatter dtf) {
        return ldt.format(dtf);
    }

    @Override
    public JkDateTime parse(String str) {
        init(LocalDateTime.parse(str, DEF_FMT));
        return this;
    }

    private void init(LocalDateTime ldt) {
        this.ldt = ldt;
    }
}
