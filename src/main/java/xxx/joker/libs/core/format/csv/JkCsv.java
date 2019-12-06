package xxx.joker.libs.core.format.csv;

import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkStrings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static xxx.joker.libs.core.format.csv.CsvConst.SEP_FIELD;
import static xxx.joker.libs.core.util.JkConvert.toList;

public class JkCsv {

    private Path csvPath;
    private List<List<String>> origData;
    private List<List<String>> currentData = new ArrayList<>();

    public JkCsv(List<String> header, List<List<String>> data) {
        List<List<String>> lines = new ArrayList<>();
        lines.add(toList(header));
        data.forEach(l -> lines.add(toList(l)));
        this.origData = lines;
        resetChanges();
    }

    public JkCsv(List<List<String>> lines) {
        this.origData = lines;
        resetChanges();
    }
    public JkCsv(List<String> lines, String fieldSep) {
        this(JkStreams.map(lines, line -> JkStrings.splitList(line, fieldSep)));
    }

    private void resetChanges() {
        currentData.clear();
        origData.forEach(line -> currentData.add(toList(line)));
    }

    public static JkCsv readFile(Path p) {
        return readFile(p, SEP_FIELD.getSeparator());
    }
    public static JkCsv readFile(Path p, String fieldSep) {
        List<String> lines = JkFiles.readLines(p);
        JkCsv csv = new JkCsv(lines, fieldSep);
        csv.csvPath = p;
        return csv;
    }

    public List<String> getHeader() {
        return currentData.isEmpty() ? Collections.emptyList() : currentData.get(0);
    }

    public List<String> strLines() {
        return JkStreams.map(currentData, l -> JkStreams.join(l, SEP_FIELD.getSeparator()));
    }

    public boolean removeCol(String colName) {
        int idx = getHeader().indexOf(colName);
        if(idx == -1)   return false;
        currentData.forEach(line -> line.remove(idx));
        return true;
    }

    public List<List<String>> getOrigData(boolean removeHeader) {
        return origData.isEmpty() ? Collections.emptyList() : origData.subList(removeHeader ? 1 : 0, origData.size());
    }
    public List<List<String>> getCurrentData(boolean removeHeader) {
        return currentData.isEmpty() ? Collections.emptyList() : currentData.subList(removeHeader ? 1 : 0, currentData.size());
    }

    public Path getCsvPath() {
        return csvPath;
    }

    public void persist(Path outPath) {
        JkFiles.writeFile(outPath, strLines());
    }
}
