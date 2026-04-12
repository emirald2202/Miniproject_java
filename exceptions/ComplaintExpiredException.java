// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception triggered when attempting to alter archived records.

package exceptions;

public class ComplaintExpiredException extends Exception {
    public ComplaintExpiredException() {
        super("This complaint is archived and cannot be modified.");
    }
}
