// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when a complaint lookup fails because the complaint does not exist.
//               Prevents null pointer / undefined behavior on critical operations.

package exceptions;

public class ComplaintNotFoundException extends Exception {

    public ComplaintNotFoundException(String message) {
        super(message);
    }

    public ComplaintNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
