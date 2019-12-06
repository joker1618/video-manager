package xxx.joker.libs.core.datetime;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class JkTimer {

    private long startTm;
    private List<Pair<String, Long>> marks;
    private long endTm;

    public JkTimer() {
        this.startTm = nowMillis();
        this.marks = new ArrayList<>();
        this.endTm = -1L;
    }
    
    public void reset() {
        startTm = nowMillis();
        marks.clear();
        endTm = -1L;
    }
    
    public void mark(String label) {
        marks.add(Pair.of(label, nowMillis()));
    }

    public long elapsed() {
        long stop = endTm == -1 ? nowMillis() : endTm;
        return stop - startTm;
    }

    public void stop() {
        if(endTm == -1) {
            endTm = nowMillis();
        }
    }

    public boolean isStopped() {
        return endTm != -1L;
    }

    public String strElapsed() {
        return JkDuration.strElapsed(elapsed());
    }

    public long getStartTm() {
        return startTm;
    }

    public long getEndTm() {
        return endTm;
    }

    public List<Pair<String, Long>> getMarks() {
        return marks;
    }

    private long nowMillis() {
        return System.currentTimeMillis();
    }

}
