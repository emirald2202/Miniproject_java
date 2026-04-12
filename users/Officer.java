// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Concrete Officer subclass demonstrating method overriding and subclass fields.

package users;

import enums.Role;
import enums.OfficerDepartment;

public class Officer extends BaseUser {
    public int assignedComplaints;
    public OfficerDepartment department;

    // Updated to include department when creating an Officer
    public Officer(int userId, String username, String password, OfficerDepartment department) {
        super(userId, username, password, Role.OFFICER);
        this.assignedComplaints = 0;
        this.department = department;
    }

    @Override
    public void performAction() {
        System.out.println("Investigating complaint as " + department + " officer");
    }
}
