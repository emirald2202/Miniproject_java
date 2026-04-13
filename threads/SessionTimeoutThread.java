



package threads;

public class SessionTimeoutThread extends Thread {

    private volatile boolean running = true;

    
    private volatile long lastActivityTime = System.currentTimeMillis();
    private volatile int timeoutSeconds;

    private final Runnable onTimeoutCallback;

    private static final int MIN_TIMEOUT_SECONDS = 10;   
    private static final int MAX_TIMEOUT_SECONDS = 300;  
    private static final int CHECK_INTERVAL_MS   = 1000; 

    public SessionTimeoutThread(int timeoutSeconds, Runnable onTimeoutCallback) {
        setTimeoutSeconds(timeoutSeconds);
        this.onTimeoutCallback = onTimeoutCallback;
    }

    
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

    
    public void resetTimer() {
        lastActivityTime = System.currentTimeMillis();
        System.out.println("[SessionTimeoutThread] Activity detected — timer reset.");
    }

    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    
    @Override
    public void run() {
        System.out.println("[SessionTimeoutThread] Started — session will expire after "
                           + timeoutSeconds + "s of inactivity.");

        while (running) {
            long idleMilliseconds = System.currentTimeMillis() - lastActivityTime;

            if (idleMilliseconds >= timeoutSeconds * 1000L) {
                System.out.println("[SessionTimeoutThread] Session timed out after "
                                   + timeoutSeconds + "s of inactivity. Logging out.");
                
                
                onTimeoutCallback.run();
                stopThread();
                break;
            }

            try {
                
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException interruptedException) {
                System.out.println("[SessionTimeoutThread] Interrupted — stopping.");
                stopThread();
            }
        }

        System.out.println("[SessionTimeoutThread] Stopped.");
    }

    
    public void stopThread() {
        running = false;
    }
}
