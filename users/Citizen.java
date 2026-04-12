// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Concrete Citizen subclass demonstrating method overriding.

package users;

import enums.Role;
import profile.CitizenProfile;

public class Citizen extends BaseUser {
    private CitizenProfile profile;

    public Citizen(int userId, String username, String password, CitizenProfile profile) {
        super(userId, username, password, Role.CITIZEN);
        this.profile = profile;
    }

    @Override
    public void performAction() {
        System.out.println("Filing a new complaint");
    }
    
    // Extractor for demonstration purposes inside main
    public CitizenProfile getProfile() {
        return this.profile;
    }
}
