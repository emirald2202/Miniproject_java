// OOP CONCEPT : Method Overloading
// ASSIGNMENT  : 3
// PURPOSE     : Multiple overloaded methods implementing conditional thrown exceptions.

package search;

import store.DataStore;
import complaints.BaseComplaint;
import enums.ComplaintCategory;
import exceptions.ComplaintNotFoundException;
import java.util.List;
import java.util.ArrayList;

public class ComplaintSearch {

    private DataStore store = DataStore.getInstance();

    private List<BaseComplaint> getAllComplaints() {
        List<BaseComplaint> all = new ArrayList<>();
        all.addAll(store.infraBox.getAll());
        all.addAll(store.corruptionBox.getAll());
        all.addAll(store.noiseBox.getAll());
        all.addAll(store.trafficBox.getAll());
        all.addAll(store.sanitationBox.getAll());
        all.addAll(store.waterSupplyBox.getAll());
        all.addAll(store.electricityBox.getAll());
        return all;
    }

    public List<BaseComplaint> search(int complaintId) throws ComplaintNotFoundException {
        List<BaseComplaint> results = new ArrayList<>();
        for (BaseComplaint c : getAllComplaints()) {
            if (c.complaintId == complaintId) {
                results.add(c);
            }
        }
        if (results.isEmpty()) throw new ComplaintNotFoundException();
        return results;
    }

    public List<BaseComplaint> search(String targetName) throws ComplaintNotFoundException {
        List<BaseComplaint> results = new ArrayList<>();
        
        for (BaseComplaint c : getAllComplaints()) {
            if (c.targetAgainst != null && c.targetAgainst.toLowerCase().contains(targetName.toLowerCase())) {
                results.add(c);
            }
        }
        
        if (results.isEmpty()) throw new ComplaintNotFoundException();
        return results;
    }

    public List<BaseComplaint> search(ComplaintCategory category) throws ComplaintNotFoundException {
        List<BaseComplaint> results = new ArrayList<>();
        switch (category) {
            case INFRASTRUCTURE: results.addAll(store.infraBox.getAll()); break;
            case CORRUPTION: results.addAll(store.corruptionBox.getAll()); break;
            case NOISE: results.addAll(store.noiseBox.getAll()); break;
            case TRAFFIC: results.addAll(store.trafficBox.getAll()); break;
            case SANITATION: results.addAll(store.sanitationBox.getAll()); break;
            case WATER_SUPPLY: results.addAll(store.waterSupplyBox.getAll()); break;
            case ELECTRICITY: results.addAll(store.electricityBox.getAll()); break;
        }
        if (results.isEmpty()) throw new ComplaintNotFoundException();
        return results;
    }
}
