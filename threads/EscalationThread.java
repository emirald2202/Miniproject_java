// OOP CONCEPT : Multithreading
// ASSIGNMENT  : 7
// PURPOSE     : Background thread that auto-escalates high-priority complaints without human intervention.

package threads;

import complaints.BaseComplaint;
import enums.Status;
import store.DataStore;

import java.util.ArrayList;
import java.util.List;

public class EscalationThread extends Thread {

    private volatile boolean running = true;
    private final DataStore store = DataStore.getInstance();

    // Score above which a complaint is automatically escalated
    // PriorityCalculator scores range 5–33 (complaintType<<2 + urgency).
    // Threshold 20 catches Water/Electricity/Sanitation/Traffic at urgency 5,
    // and Water/Electricity at any urgency.
    private static final int ESCALATION_THRESHOLD = 20;

    // How often the thread scans all complaints (in milliseconds)
    private static final int SCAN_INTERVAL_MS = 10000;

    // Collects all complaints from every ComplaintBox in DataStore into one list
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

    // Scans every active complaint and escalates those whose priority score exceeds the threshold
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
                    // Use the stored priorityScore (set by PriorityCalculator when filed)
                    int priorityScore = complaint.priorityScore;

                    if (priorityScore > ESCALATION_THRESHOLD) {
                        complaint.autoEscalate();
                        System.out.println("[EscalationThread] Auto-escalated complaint #"
                                           + complaint.complaintId
                                           + " (score=" + priorityScore + ")");

                        // Push notification so the citizen is informed via NotificationThread
                        String notification = "USERID:" + complaint.filedByUserId
                                + "|MSG:Your complaint \""
                                + complaint.title
                                + "\" has been escalated to urgent status and is being fast-tracked by higher authorities.";
                        store.notificationQueue.offer(notification);
                    }
                }
            }

            try {
                // Sleep between scans to avoid burning CPU
                Thread.sleep(SCAN_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[EscalationThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[EscalationThread] Stopped.");
    }

    // Signals the thread to exit its loop cleanly on next iteration
    public void stopThread() {
        running = false;
    }
}
