package xxx.joker.libs.core.format;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.enumerative.JkAlign;
import xxx.joker.libs.core.enumerative.JkSizeUnit;
import xxx.joker.libs.core.util.JkStrings;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import static xxx.joker.libs.core.format.csv.CsvConst.*;
import static xxx.joker.libs.core.format.csv.CsvConst.*;
/**
 * Created by f.barbano on 26/05/2018.
 */
public class JkOutput {
	
	public static String columnsView(String lines) {
		return columnsView(lines, SEP_FIELD.getSeparator(), DEF_COL_DISTANCE, false);
	}
	public static String columnsView(String lines, boolean hasHeader) {
		return columnsView(lines, SEP_FIELD.getSeparator(), DEF_COL_DISTANCE, hasHeader);
	}
	public static String columnsView(String lines, int colsDistance) {
		return columnsView(lines, SEP_FIELD.getSeparator(), colsDistance, false);
	}
	public static String columnsView(String lines, String fieldSep) {
		return columnsView(lines, fieldSep, DEF_COL_DISTANCE, false);
	}
	public static String columnsView(String lines, String fieldSep, int colsDistance) {
		return columnsView(lines, fieldSep, colsDistance, false);
	}
	public static String columnsView(String lines, String fieldSep, int colsDistance, boolean hasHeader) {
		String colsFiller = StringUtils.repeat(' ', colsDistance);
		return columnsView(lines, fieldSep, colsFiller, hasHeader);
	}
	public static String columnsView(String lines, String fieldSep, String colsFiller) {
		return columnsView(lines, fieldSep, colsFiller, false);
	}
	public static String columnsView(String lines, String fieldSep, String colsFiller, boolean hasHeader) {
		return columnsView(JkStrings.splitList(lines, StringUtils.LF), fieldSep, colsFiller, hasHeader);
	}

	public static String columnsView(List<String> lines) {
		return columnsView(lines, SEP_FIELD.getSeparator(), DEF_COL_DISTANCE, false);
	}
	public static String columnsView(List<String> lines, boolean hasHeader) {
		return columnsView(lines, SEP_FIELD.getSeparator(), DEF_COL_DISTANCE, hasHeader);
	}
	public static String columnsView(List<String> lines, int colsDistance) {
		return columnsView(lines, SEP_FIELD.getSeparator(), colsDistance, false);
	}
	public static String columnsView(List<String> lines, String fieldSep) {
		return columnsView(lines, fieldSep, DEF_COL_DISTANCE, false);
	}
	public static String columnsView(List<String> lines, String fieldSep, int colsDistance) {
		return columnsView(lines, fieldSep, colsDistance, false);
	}
	public static String columnsView(List<String> lines, String fieldSep, int colsDistance, boolean hasHeader) {
		String colsFiller = StringUtils.repeat(' ', colsDistance);
		return columnsView(lines, fieldSep, colsFiller, hasHeader);
	}
	public static String columnsView(List<String> lines, String fieldSep, String colsFiller) {
		return columnsView(lines, fieldSep, colsFiller, false);
	}
	public static String columnsView(List<String> lines, String fieldSep, String colsFiller, boolean hasHeader) {
		JkViewBuilder viewBuilder = new JkViewBuilder(lines);
		if(hasHeader) {
			viewBuilder.setHeaderAlign(JkAlign.CENTER);
		}
		viewBuilder.setDataAlign(JkAlign.LEFT);
		return viewBuilder.toString(fieldSep, colsFiller, false);
	}

	public static String humanSize(double bytes) {
		if (bytes >= JkSizeUnit.GB.size()) {
			return humanSize(bytes, JkSizeUnit.GB, false);
		} else if (bytes >= JkSizeUnit.MB.size()) {
			return humanSize(bytes, JkSizeUnit.MB, false);
		} else {
			return humanSize(bytes, JkSizeUnit.KB, false);
		}
	}
	public static String humanSize(double bytes, JkSizeUnit scale) {
		return humanSize(bytes, scale, false);
	}
	public static String humanSize(double bytes, JkSizeUnit scale, boolean roundInt) {
		double value = scale.parse(bytes);
		if(roundInt) {
			return String.format("%d %s", (int)value, scale.label());
		} else {
			return String.format("%s %s", getNumberFmtEN(2).format(value), scale.label());
		}
	}



	// Number formatter
	public static NumberFormat getNumberFmtEN(int numFractionDigits) {
		return getNumberFmtEN(numFractionDigits, numFractionDigits);
	}
	public static NumberFormat getNumberFmtEN(int minFractionDigits, int maxFractionDigits) {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMinimumFractionDigits(minFractionDigits);
		nf.setMaximumFractionDigits(maxFractionDigits);
		return nf;
	}

}
