




package exceptions;

public class DuplicateComplaintException extends Exception {

    public DuplicateComplaintException(String message) {
        super(message);
    }

    public DuplicateComplaintException(String message, Throwable cause) {
        super(message, cause);
    }
}
