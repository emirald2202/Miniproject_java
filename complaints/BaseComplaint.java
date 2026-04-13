package complaints;

import enums.Status;
import exceptions.InvalidStatusTransitionException;
import exceptions.ComplaintExpiredException;
import exceptions.OfficerNotAssignedException;
import java.time.LocalDateTime;

public abstract class BaseComplaint {
    public int complaintId;
    public String title;
    public String description;
    public Status status;
    public int filedByUserId;
    public int areaCode;
    public int urgencyLevel;
    public LocalDateTime filedDate;

    public int assignedToOfficerId;
    public int priorityScore;

    public BaseComplaint(int complaintId, String title, String description,
                         int filedByUserId, int areaCode, int urgencyLevel, LocalDateTime filedDate) {
        this.complaintId = complaintId;
        this.title = title;
        this.description = description;
        this.filedByUserId = filedByUserId;
        this.areaCode = areaCode;
        this.urgencyLevel = urgencyLevel;
        this.filedDate = filedDate;
        this.status = Status.FILED;
        this.assignedToOfficerId = -1;
        this.priorityScore = 0;
    }

    public abstract int calculatePriorityScore();

    public void updateStatus(Status newStatus)
            throws InvalidStatusTransitionException, ComplaintExpiredException {

        try {
            if (this.status == Status.RESOLVED || this.status == Status.REJECTED) {
                throw new ComplaintExpiredException(
                    "Complaint #" + complaintId + " is already " + this.status
                    + " and cannot be modified.");
            }

            boolean validTransition = false;
            switch (this.status) {
                case FILED:
                    validTransition = (newStatus == Status.UNDER_REVIEW || newStatus == Status.REJECTED);
                    break;
                case UNDER_REVIEW:
                    validTransition = (newStatus == Status.RESOLVED || newStatus == Status.ESCALATED
                                       || newStatus == Status.REJECTED);
                    break;
                case ESCALATED:
                    validTransition = (newStatus == Status.UNDER_REVIEW || newStatus == Status.RESOLVED);
                    break;
                default:
                    validTransition = false;
            }

            if (!validTransition) {
                throw new InvalidStatusTransitionException(
                    "Cannot transition complaint #" + complaintId
                    + " from " + this.status + " to " + newStatus + ".");
            }

            this.status = newStatus;

        } catch (ComplaintExpiredException | InvalidStatusTransitionException e) {
            System.err.println("[STATUS UPDATE ERROR] " + e.getMessage());
            throw e;
        }
    }

    public void autoEscalate() {
        this.status = Status.ESCALATED;
    }

    public void assignOfficer(int officerId, int requestingOfficerId)
            throws OfficerNotAssignedException {

        try {
            if (this.assignedToOfficerId != -1 && this.assignedToOfficerId != requestingOfficerId) {
                throw new OfficerNotAssignedException(
                    "Officer #" + requestingOfficerId + " is not assigned to complaint #"
                    + complaintId + ". Only officer #" + this.assignedToOfficerId + " can modify it.");
            }

            this.assignedToOfficerId = officerId;

        } catch (OfficerNotAssignedException e) {
            System.err.println("[ASSIGNMENT ERROR] " + e.getMessage());
            throw e;
        }
    }
}
