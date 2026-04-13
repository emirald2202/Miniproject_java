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

    public boolean login(String u, String p) {
        return this.username.equals(u) && this.password.equals(p);
    }

    public abstract void performAction();
}
