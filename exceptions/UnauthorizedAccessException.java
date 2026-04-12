// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when an unauthorized user tries to access restricted/sensitive data.
//               Prevents sensitive data leaks by enforcing role-based access control.

package exceptions;

public class UnauthorizedAccessException extends Exception {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
