// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception for invalid or empty input form fields.

package exceptions;

public class InvalidComplaintException extends Exception {
    public InvalidComplaintException() {
        super("Invalid complaint input. Please fill out all required fields properly.");
    }
    
    public InvalidComplaintException(String message) {
        super(message);
    }
}
