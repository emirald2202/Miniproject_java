// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Concrete Officer subclass demonstrating method overriding and subclass fields.

package users;

import enums.Role;

public class Officer extends BaseUser {
    public int assignedComplaints;

    public Officer(int userId, String username, String password) {
        super(userId, username, password, Role.OFFICER);
        this.assignedComplaints = 0;
    }

    @Override
    public void performAction() {
        System.out.println("Investigating complaint");
    }
}
