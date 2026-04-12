// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Concrete Officer subclass demonstrating method overriding and subclass fields.

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

    // Attempts to resolve a complaint — validates officer assignment and status transition
    public void resolveComplaint(BaseComplaint complaint) {
        try {
            // Check if this officer is assigned to the complaint
            if (complaint.assignedToOfficerId != this.userId) {
                throw new OfficerNotAssignedException(
                    "Officer #" + this.userId + " (" + this.username 
                    + ") is not assigned to complaint #" + complaint.complaintId + ".");
            }

            // Attempt the status transition (may throw InvalidStatusTransition or ComplaintExpired)
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

