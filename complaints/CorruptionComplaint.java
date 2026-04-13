package complaints;

import java.time.LocalDateTime;

public class CorruptionComplaint extends BaseComplaint {

    public CorruptionComplaint(int complaintId, String title, String description,
                               int filedByUserId, int areaCode, int urgencyLevel, LocalDateTime filedDate) {
        super(complaintId, title, description, filedByUserId, areaCode, urgencyLevel, filedDate);
    }

    @Override
    public int calculatePriorityScore() {
        return this.urgencyLevel * 3;
    }
}
