// OOP CONCEPT : Multithreading
// ASSIGNMENT  : 7
// PURPOSE     : Background thread that delivers queued notifications to active user dashboards.

package threads;

import store.DataStore;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationThread extends Thread {

    private volatile boolean running = true;
    private final DataStore store = DataStore.getInstance();

    // Maps a logged-in userId to a Runnable that updates their bell icon in the GUI
    private final HashMap<Integer, Runnable> activeCallbacks = new HashMap<>();

    // How often the thread polls the notification queue (in milliseconds)
    private static final int POLL_INTERVAL_MS = 1000;

    // Called by a dashboard when it opens — registers the bell-update callback for that user
    public void registerCallback(int userId, Runnable bellUpdateCallback) {
        activeCallbacks.put(userId, bellUpdateCallback);
        System.out.println("[NotificationThread] Registered callback for userId=" + userId);
    }

    // Called when a dashboard closes or user logs out — removes their callback
    public void deregisterCallback(int userId) {
        activeCallbacks.remove(userId);
        System.out.println("[NotificationThread] Deregistered callback for userId=" + userId);
    }

    // Polls the notification queue every second and delivers messages to the correct user
    @Override
    public void run() {
        System.out.println("[NotificationThread] Started — polling queue every "
                           + (POLL_INTERVAL_MS / 1000) + "s.");

        while (running) {
            // poll() is non-blocking — returns null immediately if queue is empty
            String notification = store.notificationQueue.poll();

            if (notification != null) {
                System.out.println("[NotificationThread] Received: " + notification);
                deliverNotification(notification);
            }

            try {
                // Sleep between polls to avoid burning CPU
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[NotificationThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[NotificationThread] Stopped.");
    }

    // Parses the notification string, stores the message, and triggers the GUI bell update
    private void deliverNotification(String notification) {
        // Expected format: "USERID:targetId|MSG:some message text"
        String[] parts = notification.split("\\|");

        if (parts.length < 2) {
            System.err.println("[NotificationThread] Malformed notification skipped: " + notification);
            return;
        }

        String userIdPart = parts[0]; // "USERID:5"
        String messagePart = parts[1]; // "MSG:Complaint #3 status changed to RESOLVED"

        int targetUserId;
        try {
            targetUserId = Integer.parseInt(userIdPart.split(":")[1].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException parseException) {
            System.err.println("[NotificationThread] Could not parse userId from: " + userIdPart);
            return;
        }

        String messageText = messagePart.replaceFirst("MSG:", "").trim();

        // Store message in the in-memory inbox for that user
        store.userNotifications
             .computeIfAbsent(targetUserId, userId -> new ArrayList<>())
             .add(messageText);

        System.out.println("[NotificationThread] Delivered to userId=" + targetUserId
                           + " → \"" + messageText + "\"");

        // If the target user has an open dashboard, fire their registered callback.
        // NOTE: The dashboard wraps its callback in Platform.runLater() before registering it here,
        // so JavaFX UI updates are always made on the FX thread — not this background thread.
        if (activeCallbacks.containsKey(targetUserId)) {
            Runnable bellUpdateCallback = activeCallbacks.get(targetUserId);
            bellUpdateCallback.run();
        }
    }

    // Signals the thread to exit its loop cleanly on next iteration
    public void stopThread() {
        running = false;
    }
}
