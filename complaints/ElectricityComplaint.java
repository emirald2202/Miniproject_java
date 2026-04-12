// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Electricity category of complaints with specific priority logic.

package complaints;

import java.time.LocalDateTime;

public class ElectricityComplaint extends BaseComplaint {

    public ElectricityComplaint(int complaintId, String title, String description, 
                                int filedByUserId, int areaCode, int urgencyLevel, LocalDateTime filedDate) {
        super(complaintId, title, description, filedByUserId, areaCode, urgencyLevel, filedDate);
    }

    @Override
    public int calculatePriorityScore() {
        return this.urgencyLevel * 3;
    }
}
