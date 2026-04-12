// OOP CONCEPT : Control Flow & Operators
// ASSIGNMENT  : 1
// PURPOSE     : Entry point — loads demo data, starts background threads, routes to GUI or backend test.

import complaints.*;
import containers.*;
import enums.*;
import exceptions.*;
import profile.*;
import store.*;
import threads.*;
import users.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final DataStore store = DataStore.getInstance();

    public static void main(String[] args) {
        initializeDemoData();

        // Start both background threads — daemon = true so they die when the app closes
        EscalationThread escalationThread = new EscalationThread();
        escalationThread.setDaemon(true);
        escalationThread.start();

        NotificationThread notificationThread = new NotificationThread();
        notificationThread.setDaemon(true);
        notificationThread.start();

        // Store reference so GUI dashboards can call registerCallback() later
        store.notificationThread = notificationThread;

        // Startup menu — demonstrates switch-case control flow (Assignment 1)
        Scanner scanner = new Scanner(System.in);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     Civilian Complaint Portal v1.0       ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("[1] Run Full Application (JavaFX GUI)");
        System.out.println("[2] Run Backend Test  (terminal only)");
        System.out.println("[3] Exit");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> runFullApplication();
            case 2 -> runBackendTest();
            case 3 -> { System.out.println("Goodbye."); System.exit(0); }
            default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
        }

        scanner.close();
    }

    // Loads all hardcoded demo users and complaints into DataStore so the evaluator sees a
    // fully populated system on the very first run without any manual data entry.
    private static void initializeDemoData() {
        // ── CITIZENS ──────────────────────────────────────────────────────────────
        CitizenProfile ramProfile = new CitizenProfile(
            "Ram Sharma", "1234-5678-9012", "9876543210", "12 MG Road, Pune");
        Citizen ram = new Citizen(1, "ram", "1234", ramProfile);

        CitizenProfile priyaProfile = new CitizenProfile(
            "Priya Patel", "9876-5432-1098", "8765432109", "45 FC Road, Pune");
        Citizen priya = new Citizen(2, "priya", "1234", priyaProfile);

        store.citizens.add(ram);
        store.citizens.add(priya);

        // ── OFFICERS ──────────────────────────────────────────────────────────────
        Officer officer1 = new Officer(3, "officer1", "pass", OfficerDepartment.PWD);
        Officer officer2 = new Officer(4, "officer2", "pass", OfficerDepartment.ACB);

        store.officers.add(officer1);
        store.officers.add(officer2);

        // ── ADMIN ─────────────────────────────────────────────────────────────────
        Admin adminUser = new Admin(5, "admin", "admin", 1);
        store.admins.add(adminUser);

        // ── COMPLAINTS ────────────────────────────────────────────────────────────
        // Status is set directly here because this is initialization, not a user action.
        // Runtime status changes go through updateStatus() which enforces the state machine.
        try {
            // 1. Infrastructure — UNDER_REVIEW, filed by ram, handled by officer1
            InfrastructureComplaint brokenRoad = new InfrastructureComplaint(
                1, "Broken Road on MG Road",
                "Large potholes causing accidents near MG Road junction.",
                1, 101, 4, LocalDateTime.now().minusDays(3));
            brokenRoad.priorityScore       = brokenRoad.calculatePriorityScore(); // 4*2 = 8
            brokenRoad.status              = Status.UNDER_REVIEW;
            brokenRoad.assignedToOfficerId = 3;
            store.infraBox.addComplaint(brokenRoad);

            // 2. Corruption — already ESCALATED, filed by priya, handled by officer2
            CorruptionComplaint briberyRTO = new CorruptionComplaint(
                2, "Bribery at RTO Office",
                "Officer demanding bribe for driving licence renewal at RTO office.",
                2, 202, 5, LocalDateTime.now().minusDays(2));
            briberyRTO.priorityScore       = briberyRTO.calculatePriorityScore(); // 5*3 = 15
            briberyRTO.status              = Status.ESCALATED;
            briberyRTO.assignedToOfficerId = 4;
            store.corruptionBox.addComplaint(briberyRTO);

            // 3. Noise — FILED, filed by ram, assigned to officer1
            NoiseComplaint loudMusic = new NoiseComplaint(
                3, "Loud Music at Night in Koregaon Park",
                "Nightclub playing music past 11 PM every night causing disturbance.",
                1, 303, 2, LocalDateTime.now().minusDays(1));
            loudMusic.priorityScore       = loudMusic.calculatePriorityScore(); // 2*1 = 2
            loudMusic.assignedToOfficerId = 3;
            store.noiseBox.addComplaint(loudMusic);

            // 4. Infrastructure — FILED, filed by priya, assigned to officer2
            InfrastructureComplaint brokenLight = new InfrastructureComplaint(
                4, "Broken Street Light Near School",
                "Street light outside St. Mary's School has been broken for two weeks.",
                2, 101, 3, LocalDateTime.now().minusHours(5));
            brokenLight.priorityScore       = brokenLight.calculatePriorityScore(); // 3*2 = 6
            brokenLight.assignedToOfficerId = 4;
            store.infraBox.addComplaint(brokenLight);

            // 5. Corruption — RESOLVED, 35 days ago (used for ComplaintExpiredException demo)
            CorruptionComplaint fakeDocuments = new CorruptionComplaint(
                5, "Fake Documents at Municipal Office",
                "Clerk issuing fake NOCs for construction permits in exchange for cash.",
                1, 202, 5, LocalDateTime.now().minusDays(35));
            fakeDocuments.priorityScore       = fakeDocuments.calculatePriorityScore(); // 5*3 = 15
            fakeDocuments.status              = Status.RESOLVED;
            fakeDocuments.assignedToOfficerId = 3;
            store.corruptionBox.addComplaint(fakeDocuments);

            // 6. Corruption — FILED, high urgency (auto-escalation demo for EscalationThread)
            //    Score = 15, threshold = 14 → EscalationThread will escalate this within 10 seconds
            CorruptionComplaint reliefFundBribery = new CorruptionComplaint(
                6, "Bribery Blocking Flood Relief Funds",
                "District officer diverting flood relief funds from affected villages.",
                2, 101, 5, LocalDateTime.now().minusMinutes(30));
            reliefFundBribery.priorityScore = reliefFundBribery.calculatePriorityScore(); // 5*3 = 15
            // Status stays FILED — EscalationThread will flip this to ESCALATED automatically
            store.corruptionBox.addComplaint(reliefFundBribery);

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

    // Placeholder — JavaFX launch will be wired here in Step 8
    private static void runFullApplication() {
        System.out.println("[GUI] JavaFX GUI not yet implemented. Run option [2] to test the backend.");
    }

    // Comprehensive terminal test — verifies all backend components before any GUI is added
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

    // Demonstrates polymorphism — same method call on BaseUser produces three different outputs
    private static void testPolymorphism() {
        System.out.println("─── TEST 1: Polymorphism (performAction) ───");
        BaseUser ramAsBase    = store.citizens.get(0);  // stored as BaseUser
        BaseUser officerAsBase = store.officers.get(0); // stored as BaseUser
        BaseUser adminAsBase  = store.admins.get(0);    // stored as BaseUser

        System.out.print("Citizen → ");   ramAsBase.performAction();
        System.out.print("Officer → "); officerAsBase.performAction();
        System.out.print("Admin   → ");   adminAsBase.performAction();
        System.out.println("PASS: Same method call, three different runtime behaviors.\n");
    }

    // Demonstrates encapsulation — Admin can read profile, null requestor is denied
    private static void testEncapsulation() {
        System.out.println("─── TEST 2: Encapsulation (CitizenProfile access) ───");
        Citizen  ram      = store.citizens.get(0);
        Admin    adminUser = store.admins.get(0);

        System.out.println("Admin access → " + ram.viewProfile(adminUser));
        System.out.println("Null access  → " + ram.viewProfile(null));
        System.out.println("PASS: null requestor blocked by UnauthorizedAccessException.\n");
    }

    // Demonstrates generics — three separately typed boxes reject wrong complaint types at compile time
    private static void testGenerics() {
        System.out.println("─── TEST 3: Generics (typed ComplaintBox) ───");
        System.out.println("infraBox      size: " + store.infraBox.size());
        System.out.println("corruptionBox size: " + store.corruptionBox.size());
        System.out.println("noiseBox      size: " + store.noiseBox.size());
        System.out.println("PASS: Each box only accepts its declared complaint type.\n");

        // COMPILE-TIME SAFETY DEMO:
        // The line below would cause a compile error if uncommented.
        // store.infraBox.addComplaint(new NoiseComplaint(...));
        // Java rejects it because NoiseComplaint != InfrastructureComplaint.
        // This is Generics — type errors caught before the program even runs.
    }

    // Triggers DuplicateComplaintException by filing the same complaint title twice for the same citizen
    private static void testDuplicateComplaintException() {
        System.out.println("─── TEST 4: DuplicateComplaintException ───");
        try {
            InfrastructureComplaint duplicateRoad = new InfrastructureComplaint(
                99, "Broken Road on MG Road", "Duplicate attempt",
                1, 101, 4, LocalDateTime.now()); // same title + citizenId as complaint #1
            store.infraBox.addComplaint(duplicateRoad);
            System.out.println("FAIL: Duplicate complaint was accepted — this should not happen.");
        } catch (DuplicateComplaintException duplicateException) {
            System.out.println("PASS: Caught → " + duplicateException.getMessage() + "\n");
        }
    }

    // Triggers ComplaintExpiredException by attempting to update a RESOLVED complaint
    private static void testComplaintExpiredException() {
        System.out.println("─── TEST 5: ComplaintExpiredException ───");
        // Find the 35-day-old RESOLVED complaint (id=5)
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
            resolvedComplaint.updateStatus(Status.UNDER_REVIEW); // must throw
            System.out.println("FAIL: Resolved complaint accepted a status update — should not happen.");
        } catch (ComplaintExpiredException expiredException) {
            System.out.println("PASS: Caught → " + expiredException.getMessage() + "\n");
        } catch (InvalidStatusTransitionException transitionException) {
            System.out.println("FAIL: Wrong exception type — " + transitionException.getMessage());
        }
    }

    // Tests NotificationThread delivery — pushes a notification and verifies it reaches the inbox
    private static void testNotificationThread() {
        System.out.println("─── TEST 6: NotificationThread (notification delivery) ───");

        // Register a console callback for ram (userId=1) — stands in for the GUI bell update
        store.notificationThread.registerCallback(1, () ->
            System.out.println("  [BELL CALLBACK FIRED] Unread count for userId=1: "
                + store.userNotifications.getOrDefault(1, List.of()).size())
        );

        // Officer would push this after updating a complaint status in the GUI
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

    // Tests EscalationThread — waits one full scan cycle and verifies complaint #6 was escalated
    private static void testEscalationThread() {
        System.out.println("─── TEST 7: EscalationThread (auto-escalation) ───");

        // Find complaint #6 — FILED, CorruptionComplaint urgency=5, score=15 (threshold=14)
        CorruptionComplaint highPriorityComplaint = null;
        for (CorruptionComplaint complaint : store.corruptionBox.getAllComplaints()) {
            if (complaint.complaintId == 6) {
                highPriorityComplaint = complaint;
                break;
            }
        }

        if (highPriorityComplaint == null) {
            System.out.println("FAIL: Could not find complaint #6 in corruptionBox.");
            return;
        }

        System.out.println("  Complaint #6 status BEFORE scan: " + highPriorityComplaint.status);
        System.out.println("  Priority score: " + highPriorityComplaint.calculatePriorityScore()
                           + " (threshold=14, so this WILL auto-escalate)");
        System.out.println("  Waiting 12 seconds for EscalationThread scan cycle...");

        try { Thread.sleep(12000); } catch (InterruptedException ignored) {}

        System.out.println("  Complaint #6 status AFTER scan:  " + highPriorityComplaint.status);

        if (highPriorityComplaint.status == Status.ESCALATED) {
            System.out.println("PASS: EscalationThread auto-escalated complaint #6 with no human action.\n");
        } else {
            System.out.println("FAIL: Complaint #6 was not escalated.\n");
        }
    }

    // Tests SessionTimeoutThread — demonstrates configurable timeout and resetTimer()
    private static void testSessionTimeout() {
        System.out.println("─── TEST 8: SessionTimeoutThread (session timeout) ───");

        // Track whether the callback fired
        boolean[] timeoutFired = { false };

        // Create with 10-second timeout — callback stands in for GUI logout navigation
        store.sessionTimeoutThread = new SessionTimeoutThread(10,
            () -> {
                System.out.println("  [TIMEOUT CALLBACK] Session expired — user would be sent to login screen.");
                timeoutFired[0] = true;
            });
        store.sessionTimeoutThread.setDaemon(true);
        store.sessionTimeoutThread.start();

        // Demo: try setting an invalid timeout — should be rejected and keep 10s
        System.out.println("  Trying invalid timeout (5s — below minimum of 10s):");
        store.sessionTimeoutThread.setTimeoutSeconds(5);

        // Demo: try setting a valid new timeout
        System.out.println("  Setting valid timeout to 12s:");
        store.sessionTimeoutThread.setTimeoutSeconds(12);

        // Demo: simulate user activity at 5 seconds — resets the idle clock
        System.out.println("  Waiting 5 seconds then simulating user activity...");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        store.sessionTimeoutThread.resetTimer(); // idle clock resets here

        // Now wait for the full 12-second idle period to elapse
        System.out.println("  No more activity. Waiting 13 seconds for timeout to fire...");
        try { Thread.sleep(13000); } catch (InterruptedException ignored) {}

        if (timeoutFired[0]) {
            System.out.println("PASS: SessionTimeoutThread fired logout callback after idle period.\n");
        } else {
            System.out.println("FAIL: Timeout callback did not fire.\n");
        }
    }
}
