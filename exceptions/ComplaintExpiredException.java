// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when an operation is attempted on an archived or expired complaint.
//               Enforces the system lifecycle rules — resolved/rejected complaints are immutable.

package exceptions;

public class ComplaintExpiredException extends Exception {

    public ComplaintExpiredException(String message) {
        super(message);
    }

    public ComplaintExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
