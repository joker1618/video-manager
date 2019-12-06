package xxx.joker.libs.core.exception;

import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

public interface JkThrowable {

    List<String> getCauses();

    String toStringShort();


    static String toString(Throwable t, boolean simpleClassName) {
        return JkThrowableUtil.toString(t, simpleClassName);
    }

}
