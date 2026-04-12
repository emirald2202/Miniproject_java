// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Traffic category of complaints with specific priority logic.

package complaints;

import java.time.LocalDateTime;

public class TrafficComplaint extends BaseComplaint {

    public TrafficComplaint(int complaintId, String title, String description, 
                            int filedByUserId, int areaCode, int urgencyLevel, LocalDateTime filedDate, String targetAgainst) {
        super(complaintId, title, description, filedByUserId, areaCode, urgencyLevel, filedDate, targetAgainst);
    }

    @Override
    public int calculatePriorityScore() {
        return 300;
    }
}
