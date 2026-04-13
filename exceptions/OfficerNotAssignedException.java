




package exceptions;

public class OfficerNotAssignedException extends Exception {

    public OfficerNotAssignedException(String message) {
        super(message);
    }

    public OfficerNotAssignedException(String message, Throwable cause) {
        super(message, cause);
    }
}
