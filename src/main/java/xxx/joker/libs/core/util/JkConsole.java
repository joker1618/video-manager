package xxx.joker.libs.core.util;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.lambda.JkStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.System.out;
import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Created by f.barbano on 26/05/2018.
 */

public class JkConsole {

	public static void display(Object o) {
		display("{}", o);
	}
	public static void displayColl(Collection<?> coll) {
		coll.forEach(c -> display("{}", c));
	}
	public static <T,U> void displayColl(Collection<T> coll, Function<T,U> mapper) {
		List<U> mapped = JkStreams.map(coll, mapper);
		mapped.forEach(c -> display("{}", c));
	}
	public static void display(String mexFormat, Object... params) {
		out.println(strf(mexFormat, params));
		out.flush();
	}
	public static void display(boolean newLine, String mexFormat, Object... params) {
		if(newLine) {
			out.println(strf(mexFormat, params));
		} else {
			out.print(strf(mexFormat, params));
		}
		out.flush();
	}

	public static String readUserInput(String label) {
		return readUserInput(label, true, s -> true);
	}
	public static String readUserInput(String label, Predicate<String> acceptCond) {
		return readUserInput(label, false, acceptCond);
	}
	public static String readUserInput(String label, boolean allowBlank, Predicate<String> acceptCond) {
		try {
			String heading = StringUtils.isEmpty(label) ? "" : label;

			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

			out.print(heading);
			String userInput = console.readLine();
			while (!(allowBlank && StringUtils.isBlank(userInput)) && !acceptCond.test(userInput)) {
				out.print(heading);
				userInput = console.readLine();
			}

			return userInput;

		} catch(IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}

	public static void sleep(int milli) {
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
			throw new JkRuntimeException(e);
		}
	}
}
