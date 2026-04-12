// OOP CONCEPT : Operators
// ASSIGNMENT  : 1
// PURPOSE     : Bitwise priority scoring and XOR log obfuscation.

package priority;

import enums.Status;

public class PriorityCalculator {

    public static int calculateScore(complaints.BaseComplaint complaint) {
        int baseWeight = complaint.calculatePriorityScore(); 
        int userFactor = (complaint.urgencyLevel & 0b00000111) * 10; 
        int finalScore = (baseWeight | userFactor) ^ (complaint.areaCode & 0xFF);
        return finalScore;
    }

    public static Status autoAssignStatus(int finalScore) {
        if (finalScore > 800) return Status.ESCALATED; // e.g. Corruption, Electricity
        if (finalScore > 400) return Status.UNDER_REVIEW; // e.g. Infra, Sanitation
        return Status.FILED; // e.g. Noise, Traffic
    }

    public static String obfuscateLog(String message) {
        StringBuilder sb = new StringBuilder();
        for (char c : message.toCharArray()) {
            sb.append((char) (c ^ 0b10101010));
        }
        return sb.toString();
    }

    public static String decodeLog(String encoded) {
        return obfuscateLog(encoded);
    }
}
