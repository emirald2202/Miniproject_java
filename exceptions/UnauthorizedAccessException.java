// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception triggered on unauthorized access attempts.

package exceptions;

public class UnauthorizedAccessException extends Exception {
    public UnauthorizedAccessException() {
        super("Access Denied. You do not have permission to view this resource.");
    }
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
