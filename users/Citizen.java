package users;

import enums.Role;
import profile.CitizenProfile;
import complaints.BaseComplaint;
import containers.ComplaintBox;
import exceptions.DuplicateComplaintException;
import exceptions.UnauthorizedAccessException;

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

    public CitizenProfile getProfile() {
        return this.profile;
    }

    public <T extends BaseComplaint> void fileComplaint(T complaint, ComplaintBox<T> box) {
        try {
            box.addComplaint(complaint);
            System.out.println("Complaint '" + complaint.title + "' filed successfully by " + this.username + ".");
        } catch (DuplicateComplaintException e) {
            System.err.println("[FILE COMPLAINT FAILED] " + e.getMessage());
        }
    }

    public String viewProfile(Admin admin) {
        try {
            return this.profile.getVerifiedData(admin);
        } catch (UnauthorizedAccessException e) {
            System.err.println("[ACCESS DENIED] " + e.getMessage());
            return null;
        }
    }
}
