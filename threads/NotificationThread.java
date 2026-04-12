// OOP CONCEPT : Multithreading
// ASSIGNMENT  : 7
// PURPOSE     : Background monitor tracking complaints to live-update UI.

package threads;

import store.DataStore;
import javafx.application.Platform;
import java.util.ArrayList;

public class NotificationThread extends Thread {
    private volatile boolean running = true;
    private DataStore store = DataStore.getInstance();
    public static Runnable uiCallback; // Live JavaFX UI push

    public void run() {
        while (running) {
            String notification = store.notificationQueue.poll();
            if (notification != null) {
                // Formatting exact string: USERID:targetId|MSG:msg
                try {
                    String[] parts = notification.split("\\|");
                    if (parts.length == 2 && parts[0].startsWith("USERID:")) {
                        int targetUserId = Integer.parseInt(parts[0].split(":")[1]);
                        String msg = parts[1].substring(4); // Strips "MSG:"
                        
                        store.userNotifications.putIfAbsent(targetUserId, new ArrayList<>());
                        store.userNotifications.get(targetUserId).add(msg);
                        
                        // Enforces Thread Safety in JavaFX
                        Platform.runLater(() -> {
                            System.out.println("Background Bell Triggered for User " + targetUserId + "! Message: " + msg);
                            if (uiCallback != null) {
                                uiCallback.run(); // Instantly visually reflects on frontend
                            }
                        });
                    }
                } catch (Exception e) {
                    System.out.println("Notification Parse Error: " + e.getMessage());
                }
            }
            
            try {
                Thread.sleep(1000); // Prevents thread from hoarding CPU cycles
            } catch (InterruptedException e) {
                System.out.println("Notification Thread Interrupted.");
            }
        }
    }

    public void stopThread() {
        running = false;
    }
}
