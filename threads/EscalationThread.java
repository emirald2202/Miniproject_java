



package threads;

import complaints.BaseComplaint;
import enums.Status;
import store.DataStore;

import java.util.ArrayList;
import java.util.List;

public class EscalationThread extends Thread {

    private volatile boolean running = true;
    private final DataStore store = DataStore.getInstance();

    
    
    
    
    private static final int ESCALATION_THRESHOLD = 20;

    
    private static final int SCAN_INTERVAL_MS = 10000;

    
    private List<BaseComplaint> collectAllComplaints() {
        List<BaseComplaint> allComplaints = new ArrayList<>();
        allComplaints.addAll(store.infraBox.getAllComplaints());
        allComplaints.addAll(store.corruptionBox.getAllComplaints());
        allComplaints.addAll(store.noiseBox.getAllComplaints());
        allComplaints.addAll(store.trafficBox.getAllComplaints());
        allComplaints.addAll(store.sanitationBox.getAllComplaints());
        allComplaints.addAll(store.waterSupplyBox.getAllComplaints());
        allComplaints.addAll(store.electricityBox.getAllComplaints());
        return allComplaints;
    }

    
    @Override
    public void run() {
        System.out.println("[EscalationThread] Started — scanning every "
                           + (SCAN_INTERVAL_MS / 1000) + "s for high-priority complaints.");

        while (running) {
            List<BaseComplaint> allComplaints = collectAllComplaints();

            for (BaseComplaint complaint : allComplaints) {
                boolean isActive = complaint.status == Status.FILED
                                   || complaint.status == Status.UNDER_REVIEW;

                if (isActive) {
                    
                    int priorityScore = complaint.priorityScore;

                    if (priorityScore > ESCALATION_THRESHOLD) {
                        complaint.autoEscalate();
                        System.out.println("[EscalationThread] Auto-escalated complaint #"
                                           + complaint.complaintId
                                           + " (score=" + priorityScore + ")");

                        
                        String notification = "USERID:" + complaint.filedByUserId
                                + "|MSG:Your complaint \""
                                + complaint.title
                                + "\" has been escalated to urgent status and is being fast-tracked by higher authorities.";
                        store.notificationQueue.offer(notification);
                    }
                }
            }

            try {
                
                Thread.sleep(SCAN_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[EscalationThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[EscalationThread] Stopped.");
    }

    
    public void stopThread() {
        running = false;
    }
}
