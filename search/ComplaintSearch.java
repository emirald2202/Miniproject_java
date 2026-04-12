// OOP CONCEPT : Method Overloading
// ASSIGNMENT  : 3
// PURPOSE     : Same method name handles three different search strategies at compile time.

package search;

import complaints.*;
import enums.ComplaintCategory;
import store.DataStore;
import users.Citizen;

import java.util.ArrayList;
import java.util.List;

public class ComplaintSearch {

    private final DataStore store = DataStore.getInstance();

    // COMPILE-TIME OVERLOAD RESOLUTION DEMO:
    // Java decides which search() to call based solely on the argument type — at compile time.
    // search(5)                              → search(int complaintId)
    // search("ram")                          → search(String citizenUsername)
    // search(ComplaintCategory.CORRUPTION)   → search(ComplaintCategory category)
    // No runtime checking needed — the compiler enforces it.

    // Collects every complaint from all 7 typed boxes into one flat list
    private List<BaseComplaint> getAllComplaints() {
        List<BaseComplaint> allComplaints = new ArrayList<>();
        allComplaints.addAll(store.infraBox.getAllComplaints());
        allComplaints.addAll(store.corruptionBox.getAllComplaints());
        allComplaints.addAll(store.noiseBox.getAllComplaints());
        allComplaints.addAll(store.trafficBox.getAllComplaints());
        allComplaints.addAll(store.sanitationBox.getAllComplaints());
        allComplaints.addAll(store.waterSupplyBox.getAllComplaints());
        allComplaints.addAll(store.electricityBox.getAllComplaints());
        return allComplaints;
    }

    // Overload 1 — search by complaint ID number
    public List<BaseComplaint> search(int complaintId) {
        List<BaseComplaint> results = new ArrayList<>();
        for (BaseComplaint complaint : getAllComplaints()) {
            if (complaint.complaintId == complaintId) {
                results.add(complaint);
            }
        }
        return results;
    }

    // Overload 2 — search by the username of the citizen who filed
    public List<BaseComplaint> search(String citizenUsername) {
        // First resolve username → userId via DataStore citizen list
        int targetUserId = -1;
        for (Citizen citizen : store.citizens) {
            if (citizen.username.equalsIgnoreCase(citizenUsername)) {
                targetUserId = citizen.userId;
                break;
            }
        }

        List<BaseComplaint> results = new ArrayList<>();
        if (targetUserId == -1) {
            return results; // no citizen found with that username
        }

        for (BaseComplaint complaint : getAllComplaints()) {
            if (complaint.filedByUserId == targetUserId) {
                results.add(complaint);
            }
        }
        return results;
    }

    // Overload 3 — search by complaint category (maps directly to a specific typed box)
    public List<BaseComplaint> search(ComplaintCategory category) {
        List<BaseComplaint> results = new ArrayList<>();
        switch (category) {
            case INFRASTRUCTURE -> results.addAll(store.infraBox.getAllComplaints());
            case CORRUPTION     -> results.addAll(store.corruptionBox.getAllComplaints());
            case NOISE          -> results.addAll(store.noiseBox.getAllComplaints());
            case TRAFFIC        -> results.addAll(store.trafficBox.getAllComplaints());
            case SANITATION     -> results.addAll(store.sanitationBox.getAllComplaints());
            case WATER_SUPPLY   -> results.addAll(store.waterSupplyBox.getAllComplaints());
            case ELECTRICITY    -> results.addAll(store.electricityBox.getAllComplaints());
        }
        return results;
    }
}
