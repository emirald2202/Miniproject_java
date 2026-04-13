



package search;

import complaints.*;
import enums.ComplaintCategory;
import store.DataStore;
import users.Citizen;

import java.util.ArrayList;
import java.util.List;

public class ComplaintSearch {

    private final DataStore store = DataStore.getInstance();

    
    
    
    
    
    

    
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

    
    public List<BaseComplaint> search(int complaintId) {
        List<BaseComplaint> results = new ArrayList<>();
        for (BaseComplaint complaint : getAllComplaints()) {
            if (complaint.complaintId == complaintId) {
                results.add(complaint);
            }
        }
        return results;
    }

    
    public List<BaseComplaint> search(String citizenUsername) {
        
        int targetUserId = -1;
        for (Citizen citizen : store.citizens) {
            if (citizen.username.equalsIgnoreCase(citizenUsername)) {
                targetUserId = citizen.userId;
                break;
            }
        }

        List<BaseComplaint> results = new ArrayList<>();
        if (targetUserId == -1) {
            return results; 
        }

        for (BaseComplaint complaint : getAllComplaints()) {
            if (complaint.filedByUserId == targetUserId) {
                results.add(complaint);
            }
        }
        return results;
    }

    
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
