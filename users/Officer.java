package users;

import enums.Role;
import enums.Status;
import enums.OfficerDepartment;
import complaints.BaseComplaint;
import exceptions.OfficerNotAssignedException;
import exceptions.InvalidStatusTransitionException;
import exceptions.ComplaintExpiredException;

public class Officer extends BaseUser {
    public int assignedComplaints;
    public OfficerDepartment department;

    public Officer(int userId, String username, String password, OfficerDepartment department) {
        super(userId, username, password, Role.OFFICER);
        this.assignedComplaints = 0;
        this.department = department;
    }

    @Override
    public void performAction() {
        System.out.println("Investigating complaint as " + department + " officer");
    }

    public void resolveComplaint(BaseComplaint complaint) {
        try {
            if (complaint.assignedToOfficerId != this.userId) {
                throw new OfficerNotAssignedException(
                    "Officer #" + this.userId + " (" + this.username
                    + ") is not assigned to complaint #" + complaint.complaintId + ".");
            }

            complaint.updateStatus(Status.RESOLVED);
            System.out.println("Complaint #" + complaint.complaintId + " resolved by officer " + this.username + ".");

        } catch (OfficerNotAssignedException e) {
            System.err.println("[RESOLVE DENIED] " + e.getMessage());
        } catch (InvalidStatusTransitionException e) {
            System.err.println("[RESOLVE FAILED] " + e.getMessage());
        } catch (ComplaintExpiredException e) {
            System.err.println("[RESOLVE FAILED] " + e.getMessage());
        }
    }
}
