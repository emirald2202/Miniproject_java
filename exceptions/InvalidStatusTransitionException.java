// OOP CONCEPT : Exception Handling
// ASSIGNMENT  : Custom Exceptions
// PURPOSE     : Thrown when a complaint status transition violates workflow logic.
//               Prevents the system from entering an inconsistent state
//               (e.g., moving from RESOLVED back to FILED).

package exceptions;

public class InvalidStatusTransitionException extends Exception {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
