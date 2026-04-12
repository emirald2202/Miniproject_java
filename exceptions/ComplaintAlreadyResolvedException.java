// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception triggered when attempting to alter a resolved state.

package exceptions;

public class ComplaintAlreadyResolvedException extends Exception {
    public ComplaintAlreadyResolvedException() {
        super("This complaint has already been resolved and cannot be legally altered.");
    }
}
