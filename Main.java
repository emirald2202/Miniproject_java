// PURPOSE : Main driver class to test all 6 custom exceptions and try-catch logic.

import users.*;
import complaints.*;
import containers.ComplaintBox;
import profile.CitizenProfile;
import enums.*;
import exceptions.*;

import java.time.LocalDateTime;

public class Main {

    // Helper to print section headers
    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  TEST: " + title);
        System.out.println("=".repeat(60));
    }

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       COMPLAINT MANAGEMENT SYSTEM — EXCEPTION TESTS     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        // ─── Setup test data ───────────────────────────────────────

        CitizenProfile profile1 = new CitizenProfile("Krishiv", "1234-5678-9012", "9876543210", "123 Main St");
        CitizenProfile profile2 = new CitizenProfile("Aarav", "9876-5432-1098", "9123456780", "456 Oak Ave");

        Citizen citizen1 = new Citizen(1, "krishiv", "pass123", profile1);
        Citizen citizen2 = new Citizen(2, "aarav", "pass456", profile2);

        Officer officer1 = new Officer(101, "inspector_raj", "officerpass1", OfficerDepartment.PWD);
        Officer officer2 = new Officer(102, "inspector_sam", "officerpass2", OfficerDepartment.MSEB);

        Admin admin1 = new Admin(201, "admin_chief", "adminpass", 5);

        ComplaintBox<WaterSupplyComplaint> waterBox = new ComplaintBox<>();
        ComplaintBox<ElectricityComplaint> electricityBox = new ComplaintBox<>();

        // ════════════════════════════════════════════════════════════
        // TEST 1: DuplicateComplaintException
        // ════════════════════════════════════════════════════════════
        printHeader("1. DuplicateComplaintException");
        System.out.println("Filing a water complaint...");

        WaterSupplyComplaint wc1 = new WaterSupplyComplaint(
            1001, "No Water Supply", "No water since morning",
            citizen1.userId, 400001, 5, LocalDateTime.now()
        );
        citizen1.fileComplaint(wc1, waterBox);

        System.out.println("\nFiling SAME complaint again (should trigger DuplicateComplaintException)...");
        WaterSupplyComplaint wc2 = new WaterSupplyComplaint(
            1002, "No Water Supply", "Still no water!",
            citizen1.userId, 400001, 5, LocalDateTime.now()
        );
        citizen1.fileComplaint(wc2, waterBox);

        System.out.println("\nFiling a DIFFERENT complaint by same citizen (should succeed)...");
        WaterSupplyComplaint wc3 = new WaterSupplyComplaint(
            1003, "Dirty Water", "Brown water coming from taps",
            citizen1.userId, 400001, 4, LocalDateTime.now()
        );
        citizen1.fileComplaint(wc3, waterBox);

        System.out.println("\nFiling same title by DIFFERENT citizen (should succeed)...");
        WaterSupplyComplaint wc4 = new WaterSupplyComplaint(
            1004, "No Water Supply", "No water in our area",
            citizen2.userId, 400002, 3, LocalDateTime.now()
        );
        citizen2.fileComplaint(wc4, waterBox);

        System.out.println("Water box now has " + waterBox.size() + " complaints.");

        // ════════════════════════════════════════════════════════════
        // TEST 2: UnauthorizedAccessException
        // ════════════════════════════════════════════════════════════
        printHeader("2. UnauthorizedAccessException");

        System.out.println("Admin accessing citizen profile (should succeed)...");
        String data = citizen1.viewProfile(admin1);
        if (data != null) {
            System.out.println("  Profile data: " + data);
        }

        System.out.println("\nNull requestor accessing citizen profile (should trigger UnauthorizedAccessException)...");
        String data2 = citizen1.viewProfile(null);
        if (data2 == null) {
            System.out.println("  Access correctly denied — returned null.");
        }

        // ════════════════════════════════════════════════════════════
        // TEST 3: ComplaintExpiredException
        // ════════════════════════════════════════════════════════════
        printHeader("3. ComplaintExpiredException");

        ElectricityComplaint ec1 = new ElectricityComplaint(
            2001, "Power Outage", "No electricity for 2 days",
            citizen1.userId, 400001, 5, LocalDateTime.now()
        );

        try {
            electricityBox.addComplaint(ec1);
        } catch (DuplicateComplaintException e) {
            // won't happen, first insert
        }

        System.out.println("Moving complaint through valid lifecycle: FILED → UNDER_REVIEW → RESOLVED");
        try {
            ec1.updateStatus(Status.UNDER_REVIEW);
            System.out.println("  Status after step 1: " + ec1.status);
            ec1.updateStatus(Status.RESOLVED);
            System.out.println("  Status after step 2: " + ec1.status);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            System.err.println("  Unexpected error: " + e.getMessage());
        }

        System.out.println("\nTrying to modify RESOLVED complaint (should trigger ComplaintExpiredException)...");
        try {
            ec1.updateStatus(Status.UNDER_REVIEW);
            System.out.println("  ERROR: This line should NOT print!");
        } catch (ComplaintExpiredException e) {
            System.out.println("  ✓ Correctly caught: " + e.getMessage());
        } catch (InvalidStatusTransitionException e) {
            System.out.println("  Wrong exception type caught.");
        }

        // ════════════════════════════════════════════════════════════
        // TEST 4: InvalidStatusTransitionException
        // ════════════════════════════════════════════════════════════
        printHeader("4. InvalidStatusTransitionException");

        WaterSupplyComplaint wc5 = new WaterSupplyComplaint(
            3001, "Low Pressure", "Very low water pressure",
            citizen2.userId, 400003, 3, LocalDateTime.now()
        );

        System.out.println("Attempting FILED → RESOLVED directly (should trigger InvalidStatusTransitionException)...");
        try {
            wc5.updateStatus(Status.RESOLVED);
            System.out.println("  ERROR: This line should NOT print!");
        } catch (InvalidStatusTransitionException e) {
            System.out.println("  ✓ Correctly caught: " + e.getMessage());
        } catch (ComplaintExpiredException e) {
            System.out.println("  Wrong exception type caught.");
        }

        System.out.println("\nAttempting FILED → ESCALATED directly (should trigger InvalidStatusTransitionException)...");
        try {
            wc5.updateStatus(Status.ESCALATED);
            System.out.println("  ERROR: This line should NOT print!");
        } catch (InvalidStatusTransitionException e) {
            System.out.println("  ✓ Correctly caught: " + e.getMessage());
        } catch (ComplaintExpiredException e) {
            System.out.println("  Wrong exception type caught.");
        }

        System.out.println("\nValid transition: FILED → UNDER_REVIEW...");
        try {
            wc5.updateStatus(Status.UNDER_REVIEW);
            System.out.println("  ✓ Status is now: " + wc5.status);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }

        System.out.println("Valid transition: UNDER_REVIEW → ESCALATED...");
        try {
            wc5.updateStatus(Status.ESCALATED);
            System.out.println("  ✓ Status is now: " + wc5.status);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }

        System.out.println("Valid transition: ESCALATED → RESOLVED...");
        try {
            wc5.updateStatus(Status.RESOLVED);
            System.out.println("  ✓ Status is now: " + wc5.status);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════
        // TEST 5: OfficerNotAssignedException 
        // ════════════════════════════════════════════════════════════
        printHeader("5. OfficerNotAssignedException");

        WaterSupplyComplaint wc6 = new WaterSupplyComplaint(
            4001, "Pipe Burst", "Major pipe burst on highway",
            citizen1.userId, 400001, 5, LocalDateTime.now()
        );

        // Move to UNDER_REVIEW so it can be resolved
        try {
            wc6.updateStatus(Status.UNDER_REVIEW);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            // won't happen
        }

        // Assign officer1 to the complaint
        wc6.assignedToOfficerId = officer1.userId;

        System.out.println("Officer1 (assigned) resolving complaint (should succeed)...");
        officer1.resolveComplaint(wc6);
        System.out.println("  Status: " + wc6.status);

        // Create a new complaint for the unassigned test
        WaterSupplyComplaint wc7 = new WaterSupplyComplaint(
            4002, "Leaky Valve", "Valve leaking at junction",
            citizen2.userId, 400002, 4, LocalDateTime.now()
        );

        try {
            wc7.updateStatus(Status.UNDER_REVIEW);
        } catch (InvalidStatusTransitionException | ComplaintExpiredException e) {
            // won't happen
        }

        // Assign officer1 but have officer2 try to resolve
        wc7.assignedToOfficerId = officer1.userId;

        System.out.println("\nOfficer2 (NOT assigned) trying to resolve (should trigger OfficerNotAssignedException)...");
        officer2.resolveComplaint(wc7);
        System.out.println("  Status remains: " + wc7.status);

        // Also test assignOfficer method
        System.out.println("\nTesting assignOfficer() — Officer2 trying to reassign (should trigger OfficerNotAssignedException)...");
        try {
            wc7.assignOfficer(officer2.userId, officer2.userId);
            System.out.println("  ERROR: This line should NOT print!");
        } catch (OfficerNotAssignedException e) {
            System.out.println("  ✓ Correctly caught: " + e.getMessage());
        }

        System.out.println("\nTesting assignOfficer() — Officer1 (assigned) reassigning to Officer2 (should succeed)...");
        try {
            wc7.assignOfficer(officer2.userId, officer1.userId);
            System.out.println("  ✓ Complaint now assigned to officer #" + wc7.assignedToOfficerId);
        } catch (OfficerNotAssignedException e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════
        // TEST 6: ComplaintNotFoundException
        // ════════════════════════════════════════════════════════════
        printHeader("6. ComplaintNotFoundException");

        System.out.println("Looking up existing complaint #1001 (should succeed)...");
        try {
            WaterSupplyComplaint found = waterBox.getComplaintById(1001);
            System.out.println("  ✓ Found: '" + found.title + "' (ID: " + found.complaintId + ")");
        } catch (ComplaintNotFoundException e) {
            System.out.println("  Unexpected error: " + e.getMessage());
        }

        System.out.println("\nLooking up non-existent complaint #9999 (should trigger ComplaintNotFoundException)...");
        try {
            WaterSupplyComplaint notFound = waterBox.getComplaintById(9999);
            System.out.println("  ERROR: This line should NOT print! Got: " + notFound);
        } catch (ComplaintNotFoundException e) {
            System.out.println("  ✓ Correctly caught: " + e.getMessage());
        }

        // ════════════════════════════════════════════════════════════
        // SUMMARY
        // ════════════════════════════════════════════════════════════
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ALL 6 EXCEPTION TESTS COMPLETED");
        System.out.println("=".repeat(60));
        System.out.println("  1. DuplicateComplaintException    ✓");
        System.out.println("  2. UnauthorizedAccessException    ✓");
        System.out.println("  3. ComplaintExpiredException       ✓");
        System.out.println("  4. InvalidStatusTransitionException ✓");
        System.out.println("  5. OfficerNotAssignedException    ✓");
        System.out.println("  6. ComplaintNotFoundException     ✓");
        System.out.println("=".repeat(60));
    }
}
