// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Abstract parent class representing a common user interface and properties.

package users;

import enums.Role;

public abstract class BaseUser {
    public int userId;
    public String username;
    public String password;
    public Role role;

    public BaseUser(int userId, String username, String password, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Validates if provided credentials match user's values
    public boolean login(String u, String p) {
        return this.username.equals(u) && this.password.equals(p);
    }

    public abstract void performAction();
}
