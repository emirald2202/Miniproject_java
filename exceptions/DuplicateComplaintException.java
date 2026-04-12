// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception triggered when duplicate complaints are detected.

package exceptions;

public class DuplicateComplaintException extends Exception {
    public DuplicateComplaintException() {
        super("Complaint already filed. Please wait 24 hours before resubmitting.");
    }
}
