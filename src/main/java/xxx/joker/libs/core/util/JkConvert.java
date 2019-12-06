package xxx.joker.libs.core.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by f.barbano on 29/05/2017.
 */

public class JkConvert {

	/* DATA STRUCTURES */
	@SafeVarargs
	public static <T> TreeSet<T> toTreeSet(T... source) {
		return toTreeSet(Arrays.asList(source));
	}
	public static <T> TreeSet<T> toTreeSet(Collection<T> source) {
		return source == null ? null : new TreeSet<>(source);
	}

	@SafeVarargs
	public static <T> HashSet<T> toHashSet(T... source) {
		return toHashSet(Arrays.asList(source));
	}
	public static <T> HashSet<T> toHashSet(Collection<T> source) {
		return source == null ? null : new HashSet<>(source);
	}

	@SafeVarargs
	public static <T> HashSet<T> toLinkedHashSet(T... source) {
		return toLinkedHashSet(Arrays.asList(source));
	}
	public static <T> LinkedHashSet<T> toLinkedHashSet(Collection<T> source) {
		return source == null ? null : new LinkedHashSet<>(source);
	}

	@SafeVarargs
	public static <T> List<T> toList(T... source) {
		return source == null ? null : new ArrayList<>(Arrays.asList(source));
	}
	public static <T> List<T> toList(Collection<T> source) {
		return source == null ? null : new ArrayList<>(source);
	}


	/* NUMBERS */
	public static Integer toInt(String str) {
		try {
			return Integer.valueOf(str.trim());
		} catch(Exception ex) {
			return null;
		}
	}
	public static Integer toInt(String str, Integer _default) {
        Integer num = toInt(str);
        return num == null ? _default : num;
	}
	public static Integer[] toInts(String[] source) {
		Integer[] toRet = new Integer[source.length];
		for(int i = 0; i < source.length; i++) {
			Integer num = toInt(source[i]);
			if(num == null)		return null;
			toRet[i] = num;
		}
		return toRet;
	}

	public static Long toLong(String str) {
		try {
			return Long.valueOf(str);
		} catch(Exception ex) {
			return null;
		}
	}
	public static Long toLong(String str, Long _default) {
		Long num = toLong(str);
		return num == null ? _default : num;
	}
	public static Long[] toLongs(String[] source) {
		Long[] toRet = new Long[source.length];
		for(int i = 0; i < source.length; i++) {
			Long num = toLong(source[i]);
			if(num == null)		return null;
			toRet[i] = num;
		}
		return toRet;
	}

	public static Double toDouble(String str) {
		try {
			return Double.valueOf(str.trim());
		} catch(Exception ex) {
			return null;
		}
	}
	public static Double toDouble(String str, Double _default) {
		Double num = toDouble(str);
		return num == null ? _default : num;
	}
	public static Double[] toDoubles(String[] source) {
		Double[] toRet = new Double[source.length];
		for(int i = 0; i < source.length; i++) {
			Double num = toDouble(source[i]);
			if(num == null)		return null;
			toRet[i] = num;
		}
		return toRet;
	}

	public static Float toFloat(String str) {
		try {
			return Float.valueOf(str.trim());
		} catch(Exception ex) {
			return null;
		}
	}
	public static Float toFloat(String str, Float _default) {
		Float num = toFloat(str);
		return num == null ? _default : num;
	}
	public static Float[] toFloats(String[] source) {
		Float[] toRet = new Float[source.length];
		for(int i = 0; i < source.length; i++) {
			Float num = toFloat(source[i]);
			if(num == null)		return null;
			toRet[i] = num;
		}
		return toRet;
	}

	public static boolean toBoolean(String source) {
		return source == null ? false : Boolean.valueOf(source);
	}
	public static boolean toBoolean(String source, boolean defValue) {
		return StringUtils.equalsAnyIgnoreCase(source, "true", "false") ? Boolean.valueOf(source) : defValue;
	}
	public static Boolean[] toBooleans(String[] source) {
		Boolean[] toRet = new Boolean[source.length];
		for(int i = 0; i < source.length; i++) {
			toRet[i] = Boolean.valueOf(source[i]);
		}
		return toRet;
	}


	/* PATHS Unix/Windows */
	public static String winToUnixPath(Path windowsPath) {
		return winToUnixPath(windowsPath.toString());
	}
	public static String winToUnixPath(String windowsPath) {
		return changePathFormat(windowsPath, true);
	}
	public static String[] winToUnixPath(String[] windowsPaths) {
		return changePathFormat(windowsPaths, true);
	}

	public static String unixToWinPath(Path cygwinPath) {
		return unixToWinPath(cygwinPath.toString());
	}
	public static String unixToWinPath(String cygwinPath) {
		return changePathFormat(cygwinPath, false);
	}
	public static String[] unixToWinPath(String[] cygwinPaths) {
		return changePathFormat(cygwinPaths, false);
	}

	private static String[] changePathFormat(String[] paths, boolean toCygPath) {
		String[] toRet = new String[paths.length];
		for(int i = 0; i < toRet.length; i++) {
			toRet[i] = toCygPath ? winToUnixPath(paths[i]) : unixToWinPath(paths[i]);
		}
		return toRet;
	}
	private static String changePathFormat(String sourcePath, boolean toCygPath) {
		if(sourcePath == null) {
			return null;
		}

		String toRet = "";

		if(StringUtils.isNotBlank(sourcePath)) {
			if(toCygPath) {
				toRet = sourcePath.trim().replace("\\", "/").replaceAll("/+$", "");

				if(toRet.length() >= 2) {
					char firstChar = toRet.charAt(0);

					if (Character.isAlphabetic(toRet.charAt(0)) && toRet.charAt(1) == ':') {
						String temp = "/cygdrive/" + Character.toLowerCase(firstChar);
						if(toRet.length() > 2) {
							temp += "/" + toRet.substring(2);
						}
						toRet = temp.replace("//", "/");
					}
				}
			} else {
				toRet = sourcePath.trim().replaceAll("/+$", "");

				if(toRet.startsWith("/cygdrive")) {
					toRet = toRet.replaceFirst("/cygdrive", "");
					toRet = toRet.replaceAll("^/", "");

					if(toRet.isEmpty()) {
						/**
						 * NB: assignment not totally right!!!
						 * 'cygdrive' has to be followed by a Character identifying the Volume
						 * As a workaround, will be assigned the file system root (the Volume where the O.S. reside)
						 */
						toRet = "/";
					} else {
						int idx = toRet.indexOf("/");
						String temp = idx == -1 ? "" : "/" + toRet.substring(idx+1);
						toRet = toRet.substring(0, 1).toUpperCase() + ":" + temp;
						toRet = toRet.replace("/", "\\");
					}
				}
			}
		}

		return toRet;
	}



}
