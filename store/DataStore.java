// OOP CONCEPT : Singleton Pattern
// ASSIGNMENT  : N/A
// PURPOSE     : A global single instance holding all runtime data in memory.

package store;

import users.*;
import complaints.*;
import containers.ComplaintBox;
import threads.NotificationThread;
import threads.SessionTimeoutThread;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataStore {
    private static DataStore instance;

    public List<Citizen> citizens = new ArrayList<>();
    public List<Officer> officers = new ArrayList<>();
    public List<Admin> admins = new ArrayList<>();

    public ComplaintBox<InfrastructureComplaint> infraBox;
    public ComplaintBox<CorruptionComplaint> corruptionBox;
    public ComplaintBox<NoiseComplaint> noiseBox;
    public ComplaintBox<TrafficComplaint> trafficBox;
    public ComplaintBox<SanitationComplaint> sanitationBox;
    public ComplaintBox<WaterSupplyComplaint> waterSupplyBox;
    public ComplaintBox<ElectricityComplaint> electricityBox;

    public ConcurrentLinkedQueue<String> notificationQueue = new ConcurrentLinkedQueue<>();
    public HashMap<Integer, List<String>> userNotifications = new HashMap<>();

    // Reference to the running NotificationThread so dashboards can register bell callbacks
    public NotificationThread notificationThread;

    // Reference to the active session timeout thread — replaced on every new login
    public SessionTimeoutThread sessionTimeoutThread;

    private DataStore() {
        infraBox = new ComplaintBox<>();
        corruptionBox = new ComplaintBox<>();
        noiseBox = new ComplaintBox<>();
        trafficBox = new ComplaintBox<>();
        sanitationBox = new ComplaintBox<>();
        waterSupplyBox = new ComplaintBox<>();
        electricityBox = new ComplaintBox<>();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
}
