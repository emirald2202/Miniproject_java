// OOP CONCEPT : Multithreading
// ASSIGNMENT  : 7
// PURPOSE     : Background thread that logs out an inactive user after a configurable idle period.

package threads;

public class SessionTimeoutThread extends Thread {

    private volatile boolean running = true;

    // volatile ensures the main thread's resetTimer() writes are immediately visible here
    private volatile long lastActivityTime = System.currentTimeMillis();
    private volatile int timeoutSeconds;

    private final Runnable onTimeoutCallback;

    private static final int MIN_TIMEOUT_SECONDS = 10;   // 10 seconds
    private static final int MAX_TIMEOUT_SECONDS = 300;  // 5 minutes
    private static final int CHECK_INTERVAL_MS   = 1000; // check once per second

    public SessionTimeoutThread(int timeoutSeconds, Runnable onTimeoutCallback) {
        setTimeoutSeconds(timeoutSeconds);
        this.onTimeoutCallback = onTimeoutCallback;
    }

    // Validates and updates the idle timeout — can be called live while thread is running
    public void setTimeoutSeconds(int seconds) {
        if (seconds < MIN_TIMEOUT_SECONDS || seconds > MAX_TIMEOUT_SECONDS) {
            System.err.println("[SessionTimeoutThread] Invalid timeout: " + seconds
                + "s. Must be between " + MIN_TIMEOUT_SECONDS
                + "s and " + MAX_TIMEOUT_SECONDS + "s. Keeping current value.");
            return;
        }
        this.timeoutSeconds = seconds;
        System.out.println("[SessionTimeoutThread] Timeout set to " + seconds + " seconds.");
    }

    // Called on every user action (button click, key press) to reset the idle clock
    public void resetTimer() {
        lastActivityTime = System.currentTimeMillis();
        System.out.println("[SessionTimeoutThread] Activity detected — timer reset.");
    }

    // Returns current timeout setting — used by GUI settings panel to display the value
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    // Monitors idle time every second and fires the logout callback when threshold is reached
    @Override
    public void run() {
        System.out.println("[SessionTimeoutThread] Started — session will expire after "
                           + timeoutSeconds + "s of inactivity.");

        while (running) {
            long idleMilliseconds = System.currentTimeMillis() - lastActivityTime;

            if (idleMilliseconds >= timeoutSeconds * 1000L) {
                System.out.println("[SessionTimeoutThread] Session timed out after "
                                   + timeoutSeconds + "s of inactivity. Logging out.");
                // GUI dashboards register: () -> Platform.runLater(() -> goToLoginScreen())
                // Terminal test registers a plain print statement as the callback
                onTimeoutCallback.run();
                stopThread();
                break;
            }

            try {
                // Sleep between checks to avoid burning CPU
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[SessionTimeoutThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[SessionTimeoutThread] Stopped.");
    }

    // Signals the thread to exit its loop cleanly — called on manual logout or after timeout fires
    public void stopThread() {
        running = false;
    }
}
