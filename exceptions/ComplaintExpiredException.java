




package exceptions;

public class ComplaintExpiredException extends Exception {

    public ComplaintExpiredException(String message) {
        super(message);
    }

    public ComplaintExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
