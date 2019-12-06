package xxx.joker.libs.core.exception;

import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Created by f.barbano on 19/11/2017.
 */

public class JkRuntimeException extends RuntimeException implements JkThrowable {

	private boolean simpleClassName;

	public JkRuntimeException(String message) {
		this("{}", message);
	}
	public JkRuntimeException(String message, Object... params) {
		super(strf(message, params));
	}

	public JkRuntimeException(Throwable cause, String message) {
		this(cause, "{}", message);
	}
	public JkRuntimeException(Throwable cause, String message, Object... params) {
		super(strf(message, params), cause);
	}

	public JkRuntimeException(boolean simpleClassName, Throwable cause, String message) {
		this(simpleClassName, cause, "{}", message);
	}
	public JkRuntimeException(boolean simpleClassName, Throwable cause, String message, Object... params) {
		super(strf(message, params), cause);
		this.simpleClassName = simpleClassName;
	}

	public JkRuntimeException(Throwable cause) {
		super(cause);
	}
	public JkRuntimeException(boolean simpleClassName, Throwable cause) {
		super(cause);
		this.simpleClassName = simpleClassName;
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
	public String toString() {
		return JkThrowableUtil.toString(this, simpleClassName);
	}

	@Override
	public String toStringShort() {
		return JkThrowableUtil.toStringShort(this, simpleClassName);
	}
}
