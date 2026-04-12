// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when a citizen tries to file a duplicate complaint within 24 hours.
//               Prevents data corruption from duplicate records in the system.

package exceptions;

public class DuplicateComplaintException extends Exception {

    public DuplicateComplaintException(String message) {
        super(message);
    }

    public DuplicateComplaintException(String message, Throwable cause) {
        super(message, cause);
    }
}
