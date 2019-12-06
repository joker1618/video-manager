package xxx.joker.libs.core.format;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.enumerative.JkAlign;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkStrings;

import java.util.*;

import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Created by f.barbano on 26/05/2018.
 */

public class JkViewBuilder {

	private List<String> lines;
	private JkAlign headerAlign;
	private JkAlign dataAlign;
	private Map<Integer,JkAlign> headerFieldAlign;
	private Map<Integer,JkAlign> columnFieldAlign;

	public JkViewBuilder() {
		this(new ArrayList<>());
	}
	public JkViewBuilder(List<String> lines) {
		this.lines = lines;
		this.headerAlign = null;
		this.dataAlign = JkAlign.LEFT;
		this.headerFieldAlign = new HashMap<>();
		this.columnFieldAlign = new HashMap<>();
	}

	public JkViewBuilder addLines(String format, Object... params) {
		List<String> lines = JkStrings.splitList(strf(format, params), StringUtils.LF);
		this.lines.addAll(lines);
		return this;
	}
	public JkViewBuilder addLines(int index, String format, Object... params) {
		List<String> lines = JkStrings.splitList(strf(format, params), StringUtils.LF);
		this.lines.addAll(index, lines);
		return this;
	}
	public JkViewBuilder addLines(List<String> lines) {
		this.lines.addAll(lines);
		return this;
	}
	public JkViewBuilder addLines(int index, List<String> lines) {
		this.lines.addAll(index, lines);
		return this;
	}

	public JkViewBuilder setHeaderAlign(JkAlign align, Integer... columnIndexes) {
		if(columnIndexes.length == 0) {
			headerAlign = align;
		} else {
			Arrays.stream(columnIndexes).forEach(i -> headerFieldAlign.put(i, align));
		}
		return this;
	}
	public JkViewBuilder setDataAlign(JkAlign align, Integer... columnIndexes) {
		if(columnIndexes.length == 0) {
			dataAlign = align;
		} else {
			Arrays.stream(columnIndexes).forEach(i -> columnFieldAlign.put(i, align));
		}
		return this;
	}

	public JkViewBuilder insertPrefix(String prefix, int numRepeat) {
		return insertPrefix(StringUtils.repeat(prefix, numRepeat));
	}
	public JkViewBuilder insertPrefix(String prefix) {
		lines = JkStreams.map(lines, l -> strf("%s%s", prefix, l));
		return this;
	}

	public String toString(String separator, int columnDistance) {
		return toString(separator, columnDistance, false);
	}
	public String toString(String separator, int columnDistance, boolean trimValues) {
		return toString(separator, StringUtils.repeat(" ", columnDistance), trimValues);
	}
	public String toString(String separator, String columnsSeparator) {
		return toString(separator, columnsSeparator, false);
	}
	public String toString(String separator, String columnsSeparator, boolean trimValues) {
		return JkStreams.join(toLines(separator, columnsSeparator, trimValues), StringUtils.LF);
	}

	public List<String> toLines(String separator, int columnDistance) {
		return toLines(separator, columnDistance, false);
	}
	public List<String> toLines(String separator, int columnDistance, boolean trimValues) {
		return toLines(separator, StringUtils.repeat(" ", columnDistance), trimValues);
	}
	public List<String> toLines(String separator, String columnsSeparator) {
		return toLines(separator, columnsSeparator, false);
	}
	public List<String> toLines(String separator, String columnsSeparator, boolean trimValues) {
		// Split lines in fields
		List<String[]> fieldLines = JkStreams.map(lines, line -> JkStrings.splitArr(line, separator, trimValues));

		// Fix columns number: every row must have the same number of fields
		fieldLines = adaptColumnsNumber(fieldLines);

		// Compute column descriptors
		ColDescr[] columnDescrs = computeColumnDescriptors(fieldLines);

		// create the view
		List<String> lines = new ArrayList<>();

		for(int row = 0; row < fieldLines.size(); row++) {
			List<String> fields = new ArrayList<>();
			for(int col = 0; col < columnDescrs.length; col++) {
				String str = col < fieldLines.get(row).length ? fieldLines.get(row)[col] : "";
				String field = columnDescrs[col].formatText(str, row == 0);
				fields.add(field);
			}
			lines.add(JkStreams.join(fields, columnsSeparator));
		}

		return lines;
	}

	@Override
	public String toString() {
		return JkStreams.join(lines, StringUtils.LF);
	}


	private List<String[]> adaptColumnsNumber(List<String[]> lines) {
		int numCols = lines.stream().mapToInt(arr -> arr.length).max().orElse(0);
		List<String[]> toRet = new ArrayList<>();
		for(String[] line : lines) {
			String[] row = new String[numCols];
			for(int i = 0; i < numCols; i++) {
				row[i] = i < line.length ? line[i] : "";
			}
			toRet.add(row);
		}
		return toRet;
	}
	private ColDescr[] computeColumnDescriptors(List<String[]> fieldLines) {
		// Retrieve max number of fields for one line
		int columnNumber = fieldLines.stream().mapToInt(sarr -> sarr.length).max().orElse(0);

		// Compute descriptor for each column
		ColDescr[] colDescrs = new ColDescr[columnNumber];
		for(int col = 0; col < columnNumber; col++) {
			ColDescr cd = new ColDescr();
			colDescrs[col] = cd;
			// get max column length
			int colIndex = col;
			cd.width = fieldLines.stream().mapToInt(sarr -> sarr[colIndex].length()).max().orElse(0);
			// get alignments
			cd.headerAlign = getTextAlign(col, true);
			cd.columnAlign = getTextAlign(col, false);
		}

		return colDescrs;
	}
	private JkAlign getTextAlign(int columnIndex, boolean isHeader) {
		JkAlign toRet = null;

		if(isHeader) {
			toRet = headerFieldAlign.getOrDefault(columnIndex, headerAlign);
		}

		if(toRet == null) {
			toRet = columnFieldAlign.getOrDefault(columnIndex, dataAlign);
		}

		return toRet;
	}


	private class ColDescr {
		int width;
		JkAlign headerAlign;
		JkAlign columnAlign;

		String formatText(String source, boolean isHeader) {
			JkAlign align = isHeader ? headerAlign : columnAlign;
			String toRet;
            if(width == 0) {
                toRet = strf("%s", source);
            } else if(align == JkAlign.LEFT) {
                toRet = strf("%-" + width + "s", source);
            } else if(align == JkAlign.RIGHT) {
				toRet = strf("%" + width + "s", source);
			} else {	// CENTER
				int filler = width - source.length();
				int left = filler / 2;
				toRet = strf("%s%s%s", StringUtils.repeat(" ", left), source, StringUtils.repeat(" ", filler - left));
			}
			return toRet;
		}
	}

}
