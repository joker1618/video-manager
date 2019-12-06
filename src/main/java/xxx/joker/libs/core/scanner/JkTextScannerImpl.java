package xxx.joker.libs.core.scanner;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.object.JkRange;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

class JkTextScannerImpl implements JkTextScanner {

    private String originalText;
    private StringBuilder buffer;
    private boolean ignoreCase;

    public JkTextScannerImpl(String text) {
        this(text, false);
    }
    public JkTextScannerImpl(String text, boolean ignoreCase) {
        this.originalText = text;
        this.buffer = new StringBuilder(text);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public void skip(long offset) {
        int toSkip = (int) Math.min(offset, remaining());
        toSkip = Math.max(0, toSkip);
        buffer.delete(0, toSkip);
    }

    @Override
    public long position() {
        return origLength() - remaining();
    }

    @Override
    public long remaining() {
        return buffer.length();
    }

    @Override
    public long origLength() {
        return originalText.length();
    }

    @Override
    public boolean startAt(String... toFind) {
        return setCursorMulti(true, false, true, toFind);
    }

    @Override
    public boolean startAfter(String... toFind) {
        return setCursorMulti(true, true, true, toFind);
    }

    @Override
    public boolean startAtLast(String... toFind) {
        return setCursorMulti(true, false, false, toFind);
    }

    @Override
    public boolean startAfterLast(String... toFind) {
        return setCursorMulti(true, true, false, toFind);
    }

    @Override
    public boolean endAt(String... toFind) {
        return setCursorMulti(false, false, true, toFind);
    }

    @Override
    public boolean endAfter(String... toFind) {
        return setCursorMulti(false, true, true, toFind);
    }

    @Override
    public boolean endAtLast(String... toFind) {
        return setCursorMulti(false, false, false, toFind);
    }

    @Override
    public boolean endAfterLast(String... toFind) {
        return setCursorMulti(false, true, false, toFind);
    }

    @Override
    public void reset() {
        buffer = new StringBuilder(originalText);
    }

    @Override
    public void rebaseOrigText() {
        this.originalText = buffer.toString();
    }

    @Override
    public String nextStringBetween(String start, String end) {
        return StringUtils.substringBetween(fixCase(buffer.toString()), fixCase(start), fixCase(end));
    }

    @Override
    public Integer nextIntBetween(String start, String end) {
        return JkConvert.toInt(JkStrings.safeTrim(nextStringBetween(start, end)));
    }

    @Override
    public String nextStringUntil(String end) {
        int idx = StringUtils.isEmpty(end) ? -1 : bufferIndexOf(end);
        return idx == -1 ? "" : buffer.substring(0, idx);
    }

    @Override
    public String nextString(int start, int offset) {
        return buffer.substring(start, start + offset);
    }

    @Override
    public String nextString(int offset) {
        return nextString(0, offset);
    }

    @Override
    public String nextString(JkRange range) {
        return nextString(range.getStart(), range.getLength());
    }

    @Override
    public boolean startWith(String str) {
        return bufferIndexOf(str) == 0;
    }

    @Override
    public boolean contains(String str) {
        return bufferIndexOf(str) != -1;
    }

    @Override
    public int indexOf(String str) {
        return bufferIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str) {
        return bufferLastIndexOf(str);
    }

    @Override
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }


    private boolean setCursorMulti(boolean setStart, boolean cursorAfterToFind, boolean findForward, String... toFind) {
        JkTextScannerImpl sc = new JkTextScannerImpl(buffer.toString(), ignoreCase);
        for(String findStr : toFind) {
            if(!sc.setCursor(setStart, cursorAfterToFind, findForward, findStr)) {
                return false;
            }
        }

        this.buffer = sc.buffer;
        return true;
    }
    private boolean setCursor(boolean setStart, boolean cursorAfterToFind, boolean findForward, String toFind) {
        int idx;
        if(findForward) {
            idx = bufferIndexOf(toFind);
        } else {
            idx = bufferLastIndexOf(toFind);
        }

        if(idx == -1) {
            return false;
        }

        if(cursorAfterToFind) {
            idx += toFind.length();
        }

        int begin = setStart ? 0 : idx;
        int end = setStart ? idx : buffer.length();
        buffer.delete(begin, end);

        return true;
    }

    private int bufferIndexOf(String toFind) {
        if(ignoreCase) {
            return StringUtils.indexOfIgnoreCase(buffer.toString(), toFind);
        } else {
            return buffer.toString().indexOf(toFind);
        }
    }
    private int bufferLastIndexOf(String toFind) {
        if(ignoreCase) {
            return StringUtils.lastIndexOfIgnoreCase(buffer.toString(), toFind);
        } else {
            return buffer.toString().lastIndexOf(toFind);
        }
    }

    private String fixCase(String str) {
        return ignoreCase ? str.toLowerCase() : str;
    }
}
