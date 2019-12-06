package xxx.joker.libs.core.object;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class JkRange {

    private int start;
    private int end;

    private JkRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static JkRange ofBounds(int start, int end) {
        return new JkRange(start, end);
    }
    public static JkRange ofLength(int start, int length) {
        return new JkRange(start, start + length);
    }

    @Override
    public String toString() {
        return strf("{}-{} ({})", start, end, getLength());
    }

    public JkRange shiftStart(int numShift) {
        return new JkRange(start+numShift, end+numShift);
    }
    public int getStart() {
        return start;
    }
    public int getLength() {
        return end - start;
    }
    public void setLength(int length) {
        setEnd(start + length);
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getEnd() {
        return end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
}
