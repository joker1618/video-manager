package xxx.joker.libs.core.exception;

import static xxx.joker.libs.core.util.JkStrings.strf;

abstract class JkThrowableUtil {

    public static String toString(Throwable t, boolean simpleClassName) {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringMainException(t, "", simpleClassName));
        Throwable actualThrowable = t.getCause();
        while(actualThrowable != null) {
            sb.append(toStringMainException(actualThrowable, "Caused by: ", simpleClassName));
            actualThrowable = actualThrowable.getCause();
        }

        return sb.toString().trim();
    }

    public static String toStringShort(Throwable t, boolean simpleClassName) {
        return strf("{}: {}", simpleClassName ? t.getClass().getSimpleName() : t.getClass().getName(), t.getMessage());
    }

    private static String toStringMainException(Throwable t, String mexPrefix, boolean simpleClassName) {
        StringBuilder sb = new StringBuilder();

        sb.append(mexPrefix);
        sb.append(strf("{}: {}", simpleClassName ? t.getClass().getSimpleName() : t.getClass().getName(), t.getMessage()));
        sb.append("\n");

        for(StackTraceElement elem : t.getStackTrace()) {
            sb.append("\tat ");
            sb.append(elem.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

}
