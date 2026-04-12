// OOP CONCEPT : Encapsulation & Data Hiding
// ASSIGNMENT  : 2
// PURPOSE     : Private fields with no public getters making data unreachable without permission.

package profile;

import exceptions.UnauthorizedAccessException;
import users.Admin;

public class CitizenProfile {
    private String aadhaarNumber;
    private String phoneNumber;
    private String homeAddress;
    private String fullName;

    public CitizenProfile(String fullName, String aadhaarNumber, String phoneNumber, String homeAddress) {
        this.fullName = fullName;
        this.aadhaarNumber = aadhaarNumber;
        this.phoneNumber = phoneNumber;
        this.homeAddress = homeAddress;
    }

    // Returns formatted citizen profile details to authorized admins
    public String getVerifiedData(Admin requestor) throws UnauthorizedAccessException {
        if (requestor == null) {
            throw new UnauthorizedAccessException("Only Admin can access citizen profile data.");
        }
        return "Name: " + fullName +
               " Aadhaar: " + aadhaarNumber +
               " Phone: " + phoneNumber +
               " Address: " + homeAddress;
    }
}
