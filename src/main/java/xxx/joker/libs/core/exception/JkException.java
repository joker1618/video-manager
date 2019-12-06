package xxx.joker.libs.core.exception;

import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Created by f.barbano on 19/11/2017.
 */


public class JkException extends Exception implements JkThrowable {

	private boolean simpleClassName;

	public JkException(String message, Object... params) {
		super(strf(message, params));
	}

	public JkException(boolean simpleClassName, String message, Object... params) {
		super(strf(message, params));
		this.simpleClassName = simpleClassName;
	}

	public JkException(Throwable cause, String message, Object... params) {
		super(strf(message, params), cause);
	}

	public JkException(Throwable cause) {
		super(cause);
	}

	@Override
	public List<String> getCauses() {
		List<String> causes = new ArrayList<>();
		Throwable iter = getCause();
		while (iter != null) {
			causes.add(iter.getMessage());
			iter = iter.getCause();
		}
		return causes;
	}

	@Override
	public String toStringShort() {
		return JkThrowableUtil.toStringShort(this, simpleClassName);
	}

	@Override
	public String toString() {
		return JkThrowableUtil.toString(this, simpleClassName);
	}


}
