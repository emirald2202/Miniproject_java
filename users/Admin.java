// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Concrete Admin subclass demonstrating method overriding.

package users;

import enums.Role;

public class Admin extends BaseUser {
    public int adminLevel;

    public Admin(int userId, String username, String password, int adminLevel) {
        super(userId, username, password, Role.ADMIN);
        this.adminLevel = adminLevel;
    }

    @Override
    public void performAction() {
        System.out.println("Managing and assigning");
    }
}
