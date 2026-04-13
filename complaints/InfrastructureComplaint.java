package complaints;

import java.time.LocalDateTime;

public class InfrastructureComplaint extends BaseComplaint {

    public InfrastructureComplaint(int complaintId, String title, String description,
                                   int filedByUserId, int areaCode, int urgencyLevel, LocalDateTime filedDate) {
        super(complaintId, title, description, filedByUserId, areaCode, urgencyLevel, filedDate);
    }

    @Override
    public int calculatePriorityScore() {
        return this.urgencyLevel * 2;
    }
}
