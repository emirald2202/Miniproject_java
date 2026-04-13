




package containers;

import complaints.BaseComplaint;
import exceptions.DuplicateComplaintException;
import exceptions.ComplaintNotFoundException;
import exceptions.ComplaintExpiredException;
import enums.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.time.Duration;
import java.time.LocalDateTime;

public class ComplaintBox<T extends BaseComplaint> {
    private List<T> complaints;

    public ComplaintBox() {
        this.complaints = new ArrayList<>();
    }

    
    public void addComplaint(T complaint) throws DuplicateComplaintException {
        try {
            for (T existing : complaints) {
                boolean sameCitizen = existing.filedByUserId == complaint.filedByUserId;
                boolean sameTitle = existing.title.equalsIgnoreCase(complaint.title);
                boolean within24Hours = Duration.between(existing.filedDate, complaint.filedDate).toHours() < 24;

                if (sameCitizen && sameTitle && within24Hours) {
                    throw new DuplicateComplaintException(
                        "Citizen #" + complaint.filedByUserId 
                        + " already filed a complaint titled '" + complaint.title 
                        + "' within the last 24 hours.");
                }
            }

            complaints.add(complaint);

        } catch (DuplicateComplaintException e) {
            System.err.println("[DUPLICATE ERROR] " + e.getMessage());
            throw e;
        }
    }

    
    public T getComplaintById(int complaintId) throws ComplaintNotFoundException {
        try {
            for (T complaint : complaints) {
                if (complaint.complaintId == complaintId) {
                    return complaint;
                }
            }

            throw new ComplaintNotFoundException(
                "Complaint with ID #" + complaintId + " was not found in this box.");

        } catch (ComplaintNotFoundException e) {
            System.err.println("[LOOKUP ERROR] " + e.getMessage());
            throw e;
        }
    }

    
    public List<T> getAllComplaints() {
        return complaints;
    }

    
    public void remove(int complaintId) throws ComplaintNotFoundException, ComplaintExpiredException {
        T found = getComplaintById(complaintId);

        boolean isResolved = found.status == Status.RESOLVED;
        boolean olderThan30Days = Duration.between(found.filedDate, LocalDateTime.now()).toDays() > 30;

        if (isResolved && olderThan30Days) {
            throw new ComplaintExpiredException(
                "Complaint #" + complaintId + " is archived and cannot be modified.");
        }

        complaints.remove(found);
        System.out.println("Complaint #" + complaintId + " removed successfully.");
    }

    
    public List<T> getByPriority() {
        List<T> sorted = new ArrayList<>(complaints);
        sorted.sort(Comparator.comparingInt((T c) -> c.priorityScore).reversed());
        return sorted;
    }

    
    public int size() {
        return complaints.size();
    }
}
