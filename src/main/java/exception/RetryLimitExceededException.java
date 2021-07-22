package exception;

public class RetryLimitExceededException extends RuntimeException {
    public RetryLimitExceededException(Throwable cause) {
        super(cause);
    }
}
