package xxx.joker.libs.core.scanner;

import xxx.joker.libs.core.object.JkRange;

public interface JkTextScanner {

    void skip(long offset);
    long position();
    long remaining();
    long origLength();

    boolean startAt(String... toFind);
    boolean startAfter(String... toFind);
    boolean startAtLast(String... toFind);
    boolean startAfterLast(String... toFind);

    boolean endAt(String... toFind);
    boolean endAfter(String... toFind);
    boolean endAtLast(String... toFind);
    boolean endAfterLast(String... toFind);

    void reset();
    void rebaseOrigText();

    String nextStringBetween(String start, String end);
    Integer nextIntBetween(String start, String end);
    String nextStringUntil(String end);
    String nextString(int start, int offset);
    String nextString(int offset);
    String nextString(JkRange range);

    boolean startWith(String str);
    boolean contains(String str);

    int indexOf(String str);
    int lastIndexOf(String str);

    boolean isIgnoreCase();
    void setIgnoreCase(boolean ignoreCase);

}
