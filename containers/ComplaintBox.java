// OOP CONCEPT : Generics
// ASSIGNMENT  : 5
// PURPOSE     : Type-safe container rejecting wrong complaint types at compile time.

package containers;

import complaints.BaseComplaint;
import exceptions.DuplicateComplaintException;
import exceptions.ComplaintExpiredException;
import enums.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ComplaintBox<T extends BaseComplaint> {

    private ArrayList<T> complaints = new ArrayList<>();

    public void add(T complaint) throws DuplicateComplaintException {
        // Check for duplicate before adding
        for (T existing : complaints) {
            if (existing.filedByUserId == complaint.filedByUserId && 
                existing.areaCode == complaint.areaCode && 
                existing.getClass().equals(complaint.getClass())) {
                
                long hoursBetween = Math.abs(ChronoUnit.HOURS.between(existing.filedDate, complaint.filedDate));
                if (hoursBetween < 24) {
                    throw new DuplicateComplaintException();
                }
            }
        }
        complaints.add(complaint);
    }

    public void remove(int complaintId) throws ComplaintExpiredException {
        T target = null;
        for (T c : complaints) {
            if (c.complaintId == complaintId) {
                target = c;
                break;
            }
        }

        if (target != null) {
            long daysBetween = Math.abs(ChronoUnit.DAYS.between(target.filedDate, LocalDateTime.now()));
            if (target.status == Status.RESOLVED && daysBetween > 30) {
                throw new ComplaintExpiredException();
            }
            complaints.remove(target);
        }
    }

    public List<T> getAll() {
        return complaints;
    }

    public List<T> getByPriority() {
        List<T> sorted = new ArrayList<>(complaints);
        // Descending sort by priority score
        sorted.sort(Comparator.comparingInt(c -> -c.priorityScore)); 
        return sorted;
    }
}
