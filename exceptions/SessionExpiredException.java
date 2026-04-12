// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception triggered if a user acts after system timeout.

package exceptions;

public class SessionExpiredException extends Exception {
    public SessionExpiredException() {
        super("Your session has expired due to inactivity. Please log in again.");
    }
}
