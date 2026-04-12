// OOP CONCEPT : Custom Exceptions
// ASSIGNMENT  : 6
// PURPOSE     : Custom exception for when a search yields zero results.

package exceptions;

public class ComplaintNotFoundException extends Exception {
    public ComplaintNotFoundException() {
        super("No complaints matching your search criteria were found.");
    }
}
