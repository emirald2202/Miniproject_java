// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when an officer tries to modify a complaint they are not assigned to.
//               Prevents unauthorized modification — only the assigned officer can act.

package exceptions;

public class OfficerNotAssignedException extends Exception {

    public OfficerNotAssignedException(String message) {
        super(message);
    }

    public OfficerNotAssignedException(String message, Throwable cause) {
        super(message, cause);
    }
}
