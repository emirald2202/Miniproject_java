// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Abstract parent class for common complaint properties and methods.

package complaints;

import enums.Status;
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

    // Updates the current status of the complaint
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
    }
}
