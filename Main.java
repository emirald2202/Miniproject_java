// OOP CONCEPT : Application Entry
// ASSIGNMENT  : N/A
// PURPOSE     : Central launcher integrating DataStore, Threads, and JavaFX loop.

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import store.DataStore;
import users.*;
import complaints.*;
import enums.*;
import profile.CitizenProfile;
import gui.LoginScreen;
import threads.NotificationThread;

import java.time.LocalDateTime;

public class Main extends Application {

    private static NotificationThread notifThread;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Booting System...");
        initializeDemoData();

        // Fire daemon Multithreading hook
        notifThread = new NotificationThread();
        notifThread.setDaemon(true);
        notifThread.start();

        System.out.println("Spawning JavaFX Windows...");
        Scene loginScene = LoginScreen.getScene(primaryStage);
        primaryStage.setTitle("Civilian Complaint Portal");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (notifThread != null) {
            notifThread.stopThread();
        }
    }

    public static void initializeDemoData() {
        DataStore store = DataStore.getInstance();

        // Adding exact demo data instructed cleanly
        Citizen ram = new Citizen(1, "ram", "1234", new CitizenProfile("Ram Kumar", "1234", "999", "123 ABC St"));
        Citizen priya = new Citizen(2, "priya", "1234", new CitizenProfile("Priya Sharma", "9876", "888", "456 DEF Rd"));
        store.citizens.add(ram);
        store.citizens.add(priya);
        store.officers.add(new Officer(101, "officer1", "pass", OfficerDepartment.PWD));
        store.officers.add(new Officer(102, "officer2", "pass", OfficerDepartment.ACB));
        store.officers.add(new Officer(103, "sanitation_officer", "pass", OfficerDepartment.MUNICIPAL_CORPORATION));
        store.admins.add(new Admin(999, "admin", "admin", 1));

        try {
            InfrastructureComplaint c1 = new InfrastructureComplaint(1, "Broken Road", "Potholes", ram.userId, 101, 4, LocalDateTime.now(), "Municipal Road Works");
            c1.priorityScore = priority.PriorityCalculator.calculateScore(c1);
            c1.status = priority.PriorityCalculator.autoAssignStatus(c1.priorityScore);
            store.infraBox.add(c1);

            CorruptionComplaint c2 = new CorruptionComplaint(2, "Bribery at RTO Office", "Demanding bribes", priya.userId, 202, 5, LocalDateTime.now(), "RTO Clerk Ramesh");
            c2.priorityScore = priority.PriorityCalculator.calculateScore(c2);
            c2.status = Status.ESCALATED;
            store.corruptionBox.add(c2);
            
            NoiseComplaint c3 = new NoiseComplaint(3, "Loud Music at Night", "DJ", ram.userId, 303, 2, LocalDateTime.now(), "Club ABC");
            c3.priorityScore = priority.PriorityCalculator.calculateScore(c3);
            c3.status = priority.PriorityCalculator.autoAssignStatus(c3.priorityScore);
            store.noiseBox.add(c3);

            InfrastructureComplaint c4 = new InfrastructureComplaint(4, "Broken Street Light", "Dark", priya.userId, 101, 3, LocalDateTime.now(), "Electric Board local ward");
            c4.priorityScore = priority.PriorityCalculator.calculateScore(c4);
            c4.status = priority.PriorityCalculator.autoAssignStatus(c4.priorityScore);
            store.infraBox.add(c4);

            CorruptionComplaint c5 = new CorruptionComplaint(5, "Fake Documents", "Fraud", ram.userId, 202, 5, LocalDateTime.now().minusDays(35), "Agent Amit");
            c5.priorityScore = priority.PriorityCalculator.calculateScore(c5);
            c5.status = Status.RESOLVED; 
            store.corruptionBox.add(c5);
            
        } catch (Exception e) {
            System.err.println("Load Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
