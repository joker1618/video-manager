package xxx.joker.libs.core.util;

//import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.lambda.JkStreams;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by f.barbano on 25/05/2018.
 */

public class JkStrings {

	/**
	 * Can be used in 2 different forms:
	 * 1) using placeholders like String.format (%s, %d, ...)
	 * 2) using placeholders like Logger ({})
	 *
	 * Count string 'format' occurrences of String.format placeholders (a) and {} placeholders (b):
	 * if the number of (a) does not match the number of params and (b) match --> use (b) placeholder
	 * else  --> use (a) placeholder
	 */
	public static String strf(String format, Object... params) {
		if(params.length == 0) {
			return format;
		}

		String toRet;
		format = format.replace("%n", StringUtils.LF);

		int numPhString = countPlaceholders(format, false);
		int numPhLogger = countPlaceholders(format, true);

		if(numPhLogger > 0 && numPhLogger != numPhString && (numPhString == 0 || numPhLogger == params.length)) {
			toRet = strLog(format, params);
		} else {
			toRet = strFmt(format, params);
		}

		return toRet;
	}
	public static String strf(Object obj) {
		return strLog("{}", obj);
	}
	// Use String.format placeholders  (%s, %d, ...)
	public static String strFmt(String format, Object... params) {
		return params.length == 0 ? format : String.format(format, params);
	}
	// Use logger placeholders  ({})
	public static String strLog(String format, Object... params) {
		if(params.length == 0) {
			return format;
		}

		StringBuilder sb = new StringBuilder();

		List<String> splits = JkStrings.splitList(format, "{}");
		if(!splits.isEmpty()) {
			int splitPos = 0;
			sb.append(splits.get(splitPos++));
			for(int i = 0; splitPos < splits.size() && i < params.length; i++) {
				Object obj = params[i];
				String strValue;
				if(obj == null)	{
					strValue = "_null";
				} else if(obj.getClass().isArray()){
					strValue = Arrays.toString((Object[])obj);
				} else {
					strValue = obj.toString();
				}
				sb.append(strValue).append(splits.get(splitPos++));
			}
			for(; splitPos < splits.size(); ) {
				sb.append("{}").append(splits.get(splitPos++));
			}
		}

		return sb.toString();
	}

	public static String[] splitArr(String source, String separatorString) {
		return splitArr(source, separatorString, false);
	}
	public static String[] splitArr(String source, String separatorString, boolean trimValues) {
		return splitArr(source, separatorString, trimValues, true);
	}
	public static String[] splitArr(String source, String separatorString, boolean trimValues, boolean removeSeparator) {
		if(StringUtils.isEmpty(source)) {
			return new String[0];
		}

		String[] splitted = source.split(Pattern.quote(separatorString));
		int numFields = StringUtils.countMatches(source, separatorString) + 1;

		String[] toRet = new String[numFields];

		int pos = 0;
		for(; pos < splitted.length; pos++) {
			String str = removeSeparator || pos == numFields-1 ? splitted[pos] : splitted[pos] + separatorString;
			toRet[pos] = trimValues ? str.trim() : str;
		}
		for(; pos < numFields; pos++) {
			toRet[pos] = "";
		}

		return toRet;
	}

	public static List<String> splitList(String source, String separatorString) {
		return splitList(source, separatorString, false);
	}
	public static List<String> splitList(String source, String separatorString, boolean trimValues) {
		return splitList(source, separatorString, trimValues, true);
	}
	public static List<String> splitList(String source, String separatorString, boolean trimValues, boolean removeSeparator) {
		return JkConvert.toList(splitArr(source, separatorString, trimValues, removeSeparator));
	}

	public static String leftPadLines(String source, String padStr) {
		return leftPadLines(source, padStr, 1);
	}
	public static String leftPadLines(String source, String padStr, int padSize) {
		String[] lines = splitArr(source, "\n");
		List<String> padded = leftPadLines(JkConvert.toList(lines), padStr, padSize);
		return JkStreams.join(padded, "\n");
	}
	public static List<String> leftPadLines(List<String> list, String padStr) {
		return leftPadLines(list, padStr, 1);
	}
	public static List<String> leftPadLines(List<String> list, String padStr, int padSize) {
		return list.stream().map(str -> StringUtils.repeat(padStr, padSize) + str).collect(Collectors.toList());
	}

	public static String safeTrim(String source) {
		return safeTrim(source, "");
	}
	public static String safeTrim(String source, String _default) {
		return StringUtils.isBlank(source) ? _default : source.trim();
	}

	public static List<String> splitFlat(Collection<String> tags) {
		Set<String> tagSet = new HashSet<>();
		for (String tag : tags) {
			List<String> tlist = JkConvert.toList(tag.split("[\\s,;|]"));
			tlist.removeIf(StringUtils::isBlank);
			tagSet.addAll(tlist);
		}
		return JkConvert.toList(tagSet);
	}
	public static List<String> splitFlat(String... tags) {
		return splitFlat(JkConvert.toList(tags));
	}


	public static boolean matchRegExp(String regex, String source) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		return matcher.matches();
	}

	public static String unescapeHtmlSpecialChars(String htmlText) {
		// Custom replacements before undecoding using StringUtils
		htmlText = htmlText.replace("&#160;", "");  // Non-breaking space

		htmlText = StringEscapeUtils.unescapeHtml4(htmlText);
		return htmlText;
	}

	private static int countPlaceholders(String str, boolean logStyle) {
		// %[argument_index$][flags][width][.precision][t]conversion
		String fmtString = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
		String fmtLogger = "\\{}";

		Pattern pattern = Pattern.compile(logStyle ? fmtLogger : fmtString);
		Matcher matcher = pattern.matcher(str);

		int counter = 0;
		while (matcher.find()) {
			counter++;
		}

		return counter;
	}




}
