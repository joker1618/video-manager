package xxx.joker.libs.repo.exceptions;

import xxx.joker.libs.core.exception.JkRuntimeException;

import java.util.List;

public class RepoError extends JkRuntimeException {

    private ErrorType errorType;

//    public RepoError(String message) {
//        super(message);
//    }
//
//    public RepoError(String message, Object... params) {
//        super(message, params);
//    }

    public RepoError(ErrorType errorType, String message, Object... params) {
        super(message, params);
        this.errorType = errorType;
    }

//    public RepoError(Throwable cause, String message, Object... params) {
//        super(cause, message, params);
//    }
//
//    public RepoError(Throwable cause) {
//        super(cause);
//    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
