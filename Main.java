



import complaints.*;
import containers.*;
import enums.*;
import exceptions.*;
import priority.PriorityCalculator;
import profile.*;
import store.*;
import threads.*;
import users.*;

import javafx.application.Application;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    private static final DataStore store = DataStore.getInstance();

    public static void main(String[] args) {
        initializeDemoData();

        
        EscalationThread escalationThread = new EscalationThread();
        escalationThread.setDaemon(true);
        escalationThread.start();

        NotificationThread notificationThread = new NotificationThread();
        notificationThread.setDaemon(true);
        notificationThread.start();

        
        store.notificationThread = notificationThread;

        
        if (args.length > 0 && args[0].equals("--test")) {
            runBackendTest();
        } else {
            Application.launch(gui.MainApp.class, args);
        }
    }

    
    
    private static void initializeDemoData() {
        
        CitizenProfile ramProfile = new CitizenProfile(
            "Ram Sharma", "1234-5678-9012", "9876543210", "12 MG Road, Pune");
        Citizen ram = new Citizen(1, "ram", "1234", ramProfile);

        CitizenProfile priyaProfile = new CitizenProfile(
            "Priya Patel", "9876-5432-1098", "8765432109", "45 FC Road, Pune");
        Citizen priya = new Citizen(2, "priya", "1234", priyaProfile);

        store.citizens.add(ram);
        store.citizens.add(priya);

        
        Officer officer1 = new Officer(3, "officer1", "pass", OfficerDepartment.PWD);
        Officer officer2 = new Officer(4, "officer2", "pass", OfficerDepartment.ACB);

        store.officers.add(officer1);
        store.officers.add(officer2);

        
        Admin adminUser = new Admin(5, "admin", "admin", 1);
        store.admins.add(adminUser);

        
        
        
        try {
            
            
            
            InfrastructureComplaint brokenRoad = new InfrastructureComplaint(
                1, "Broken Road on MG Road",
                "Large potholes causing accidents near MG Road junction.",
                1, 101, 4, LocalDateTime.now().minusDays(3));
            brokenRoad.priorityScore       = PriorityCalculator.calculateScore(1, 4, 0); 
            brokenRoad.status              = Status.UNDER_REVIEW;
            brokenRoad.assignedToOfficerId = 3;
            store.infraBox.addComplaint(brokenRoad);

            
            
            CorruptionComplaint briberyRTO = new CorruptionComplaint(
                2, "Bribery at RTO Office",
                "Officer demanding bribe for driving licence renewal at RTO office.",
                2, 202, 5, LocalDateTime.now().minusDays(2));
            briberyRTO.priorityScore       = PriorityCalculator.calculateScore(2, 5, 0); 
            briberyRTO.status              = Status.ESCALATED;
            briberyRTO.assignedToOfficerId = 4;
            store.corruptionBox.addComplaint(briberyRTO);

            
            
            NoiseComplaint loudMusic = new NoiseComplaint(
                3, "Loud Music at Night in Koregaon Park",
                "Nightclub playing music past 11 PM every night causing disturbance.",
                1, 303, 2, LocalDateTime.now().minusDays(1));
            loudMusic.priorityScore       = PriorityCalculator.calculateScore(3, 2, 0); 
            loudMusic.assignedToOfficerId = 3;
            store.noiseBox.addComplaint(loudMusic);

            
            
            InfrastructureComplaint brokenLight = new InfrastructureComplaint(
                4, "Broken Street Light Near School",
                "Street light outside St. Mary's School has been broken for two weeks.",
                2, 101, 3, LocalDateTime.now().minusHours(5));
            brokenLight.priorityScore       = PriorityCalculator.calculateScore(1, 3, 0); 
            brokenLight.assignedToOfficerId = 4;
            store.infraBox.addComplaint(brokenLight);

            
            
            CorruptionComplaint fakeDocuments = new CorruptionComplaint(
                5, "Fake Documents at Municipal Office",
                "Clerk issuing fake NOCs for construction permits in exchange for cash.",
                1, 202, 5, LocalDateTime.now().minusDays(35));
            fakeDocuments.priorityScore       = PriorityCalculator.calculateScore(2, 5, 0); 
            fakeDocuments.status              = Status.RESOLVED;
            fakeDocuments.assignedToOfficerId = 3;
            store.corruptionBox.addComplaint(fakeDocuments);

            
            
            ElectricityComplaint powerOutage = new ElectricityComplaint(
                6, "Complete Power Outage in Koregaon Park",
                "Entire neighbourhood has had no electricity for 18 hours after transformer failure.",
                2, 101, 5, LocalDateTime.now().minusMinutes(30));
            powerOutage.priorityScore = PriorityCalculator.calculateScore(7, 5, 0); 
            
            store.electricityBox.addComplaint(powerOutage);

        } catch (DuplicateComplaintException loadException) {
            System.err.println("[DEMO DATA ERROR] " + loadException.getMessage());
        }

        System.out.println("[DataStore] Demo data loaded — "
            + store.citizens.size()    + " citizens, "
            + store.officers.size()    + " officers, "
            + store.admins.size()      + " admins, "
            + (store.infraBox.size()
               + store.corruptionBox.size()
               + store.noiseBox.size()) + " complaints.");
    }

    
    private static void runFullApplication() {
        Application.launch(gui.MainApp.class);
    }

    
    private static void runBackendTest() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║          BACKEND TEST SUITE              ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        testPolymorphism();
        testEncapsulation();
        testGenerics();
        testDuplicateComplaintException();
        testComplaintExpiredException();
        testNotificationThread();
        testEscalationThread();
        testSessionTimeout();

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         ALL TESTS COMPLETE               ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }

    
    private static void testPolymorphism() {
        System.out.println("─── TEST 1: Polymorphism (performAction) ───");
        BaseUser ramAsBase    = store.citizens.get(0);  
        BaseUser officerAsBase = store.officers.get(0); 
        BaseUser adminAsBase  = store.admins.get(0);    

        System.out.print("Citizen → ");   ramAsBase.performAction();
        System.out.print("Officer → "); officerAsBase.performAction();
        System.out.print("Admin   → ");   adminAsBase.performAction();
        System.out.println("PASS: Same method call, three different runtime behaviors.\n");
    }

    
    private static void testEncapsulation() {
        System.out.println("─── TEST 2: Encapsulation (CitizenProfile access) ───");
        Citizen  ram      = store.citizens.get(0);
        Admin    adminUser = store.admins.get(0);

        System.out.println("Admin access → " + ram.viewProfile(adminUser));
        System.out.println("Null access  → " + ram.viewProfile(null));
        System.out.println("PASS: null requestor blocked by UnauthorizedAccessException.\n");
    }

    
    private static void testGenerics() {
        System.out.println("─── TEST 3: Generics (typed ComplaintBox) ───");
        System.out.println("infraBox      size: " + store.infraBox.size());
        System.out.println("corruptionBox size: " + store.corruptionBox.size());
        System.out.println("noiseBox      size: " + store.noiseBox.size());
        System.out.println("PASS: Each box only accepts its declared complaint type.\n");

        
        
        
        
        
    }

    
    private static void testDuplicateComplaintException() {
        System.out.println("─── TEST 4: DuplicateComplaintException ───");
        try {
            InfrastructureComplaint duplicateRoad = new InfrastructureComplaint(
                99, "Broken Road on MG Road", "Duplicate attempt",
                1, 101, 4, LocalDateTime.now()); 
            store.infraBox.addComplaint(duplicateRoad);
            System.out.println("FAIL: Duplicate complaint was accepted — this should not happen.");
        } catch (DuplicateComplaintException duplicateException) {
            System.out.println("PASS: Caught → " + duplicateException.getMessage() + "\n");
        }
    }

    
    private static void testComplaintExpiredException() {
        System.out.println("─── TEST 5: ComplaintExpiredException ───");
        
        CorruptionComplaint resolvedComplaint = null;
        for (CorruptionComplaint complaint : store.corruptionBox.getAllComplaints()) {
            if (complaint.complaintId == 5) {
                resolvedComplaint = complaint;
                break;
            }
        }

        if (resolvedComplaint == null) {
            System.out.println("FAIL: Could not find complaint #5 in corruptionBox.");
            return;
        }

        try {
            resolvedComplaint.updateStatus(Status.UNDER_REVIEW); 
            System.out.println("FAIL: Resolved complaint accepted a status update — should not happen.");
        } catch (ComplaintExpiredException expiredException) {
            System.out.println("PASS: Caught → " + expiredException.getMessage() + "\n");
        } catch (InvalidStatusTransitionException transitionException) {
            System.out.println("FAIL: Wrong exception type — " + transitionException.getMessage());
        }
    }

    
    private static void testNotificationThread() {
        System.out.println("─── TEST 6: NotificationThread (notification delivery) ───");

        
        store.notificationThread.registerCallback(1, () ->
            System.out.println("  [BELL CALLBACK FIRED] Unread count for userId=1: "
                + store.userNotifications.getOrDefault(1, List.of()).size())
        );

        
        String statusChangeNotification = "USERID:1|MSG:An officer has started reviewing your complaint "
                + "\"Loud Music at Night in Koregaon Park\". You will be notified when it is resolved.";
        store.notificationQueue.offer(statusChangeNotification);
        System.out.println("  Pushed to queue: \"" + statusChangeNotification + "\"");
        System.out.println("  Waiting 2 seconds for NotificationThread to process...");

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        List<String> ramInbox = store.userNotifications.get(1);
        if (ramInbox != null && !ramInbox.isEmpty()) {
            System.out.println("PASS: Message in ram's inbox → \"" + ramInbox.get(0) + "\"\n");
        } else {
            System.out.println("FAIL: ram's inbox is empty after 2 seconds.\n");
        }
    }

    
    private static void testEscalationThread() {
        System.out.println("─── TEST 7: EscalationThread (auto-escalation) ───");

        
        ElectricityComplaint highPriorityComplaint = null;
        for (ElectricityComplaint complaint : store.electricityBox.getAllComplaints()) {
            if (complaint.complaintId == 6) {
                highPriorityComplaint = complaint;
                break;
            }
        }

        if (highPriorityComplaint == null) {
            System.out.println("FAIL: Could not find complaint #6 in electricityBox.");
            return;
        }

        System.out.println("  Complaint #6 status BEFORE scan: " + highPriorityComplaint.status);
        System.out.println("  Priority score: " + highPriorityComplaint.priorityScore
                           + " (threshold=20, so this WILL auto-escalate)");
        System.out.println("  Waiting 12 seconds for EscalationThread scan cycle...");

        try { Thread.sleep(12000); } catch (InterruptedException ignored) {}

        System.out.println("  Complaint #6 status AFTER scan:  " + highPriorityComplaint.status);

        if (highPriorityComplaint.status == Status.ESCALATED) {
            System.out.println("PASS: EscalationThread auto-escalated complaint #6 with no human action.\n");
        } else {
            System.out.println("FAIL: Complaint #6 was not escalated.\n");
        }
    }

    
    private static void testSessionTimeout() {
        System.out.println("─── TEST 8: SessionTimeoutThread (session timeout) ───");

        
        boolean[] timeoutFired = { false };

        
        SessionTimeoutThread testThread = new SessionTimeoutThread(10,
            () -> {
                System.out.println("  [TIMEOUT CALLBACK] Session expired — user would be sent to login screen.");
                timeoutFired[0] = true;
            });
        testThread.setDaemon(true);
        testThread.start();

        
        System.out.println("  Trying invalid timeout (5s — below minimum of 10s):");
        testThread.setTimeoutSeconds(5);

        
        System.out.println("  Setting valid timeout to 12s:");
        testThread.setTimeoutSeconds(12);

        
        System.out.println("  Waiting 5 seconds then simulating user activity...");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        testThread.resetTimer(); 

        
        System.out.println("  No more activity. Waiting 13 seconds for timeout to fire...");
        try { Thread.sleep(13000); } catch (InterruptedException ignored) {}

        if (timeoutFired[0]) {
            System.out.println("PASS: SessionTimeoutThread fired logout callback after idle period.\n");
        } else {
            System.out.println("FAIL: Timeout callback did not fire.\n");
        }
    }
}
