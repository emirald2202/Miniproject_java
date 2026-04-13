



package threads;

import store.DataStore;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationThread extends Thread {

    private volatile boolean running = true;
    private final DataStore store = DataStore.getInstance();

    
    private final HashMap<Integer, Runnable> activeCallbacks = new HashMap<>();

    
    private static final int POLL_INTERVAL_MS = 1000;

    
    public void registerCallback(int userId, Runnable bellUpdateCallback) {
        activeCallbacks.put(userId, bellUpdateCallback);
        System.out.println("[NotificationThread] Registered callback for userId=" + userId);
    }

    
    public void deregisterCallback(int userId) {
        activeCallbacks.remove(userId);
        System.out.println("[NotificationThread] Deregistered callback for userId=" + userId);
    }

    
    @Override
    public void run() {
        System.out.println("[NotificationThread] Started — polling queue every "
                           + (POLL_INTERVAL_MS / 1000) + "s.");

        while (running) {
            
            String notification = store.notificationQueue.poll();

            if (notification != null) {
                System.out.println("[NotificationThread] Received: " + notification);
                deliverNotification(notification);
            }

            try {
                
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[NotificationThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[NotificationThread] Stopped.");
    }

    
    private void deliverNotification(String notification) {
        
        String[] parts = notification.split("\\|");

        if (parts.length < 2) {
            System.err.println("[NotificationThread] Malformed notification skipped: " + notification);
            return;
        }

        String userIdPart = parts[0]; 
        String messagePart = parts[1]; 

        int targetUserId;
        try {
            targetUserId = Integer.parseInt(userIdPart.split(":")[1].trim());
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException parseException) {
            System.err.println("[NotificationThread] Could not parse userId from: " + userIdPart);
            return;
        }

        String messageText = messagePart.replaceFirst("MSG:", "").trim();

        
        store.userNotifications
             .computeIfAbsent(targetUserId, userId -> new ArrayList<>())
             .add(messageText);

        System.out.println("[NotificationThread] Delivered to userId=" + targetUserId
                           + " → \"" + messageText + "\"");

        
        
        
        if (activeCallbacks.containsKey(targetUserId)) {
            Runnable bellUpdateCallback = activeCallbacks.get(targetUserId);
            bellUpdateCallback.run();
        }
    }

    
    public void stopThread() {
        running = false;
    }
}
