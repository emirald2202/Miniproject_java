// OOP CONCEPT : Operators & Control Flow
// ASSIGNMENT  : 1
// PURPOSE     : Calculates complaint priority using bitwise operators and XOR log obfuscation.

package priority;

import enums.Status;

public class PriorityCalculator {

    // Complaint type integer mapping (as specified in assignment)
    public static final int TYPE_INFRASTRUCTURE = 1;
    public static final int TYPE_CORRUPTION     = 2;
    public static final int TYPE_NOISE          = 3;
    public static final int TYPE_TRAFFIC        = 4;
    public static final int TYPE_SANITATION     = 5;
    public static final int TYPE_WATER_SUPPLY   = 6;
    public static final int TYPE_ELECTRICITY    = 7;

    // XOR key used for log obfuscation and decoding
    private static final int XOR_KEY = 0b10101010;

    // Calculates the final priority score using bitwise shift, OR, and XOR operations
    public static int calculateScore(int complaintType, int urgencyLevel, int areaCode) {
        int rawScore   = (complaintType << 2) | (urgencyLevel & 0b00000111);
        int finalScore = rawScore ^ (areaCode & 0xFF);
        return finalScore;
    }

    // Assigns a Status automatically based on the calculated priority score
    public static Status autoAssignStatus(int finalScore) {
        if (finalScore > 15) {
            return Status.ESCALATED;
        } else if (finalScore > 8) {
            return Status.UNDER_REVIEW;
        } else {
            return Status.FILED;
        }
    }

    // XORs every character in the message with the key — produces unreadable scrambled string
    public static String obfuscateLog(String message) {
        StringBuilder scrambled = new StringBuilder();
        for (char character : message.toCharArray()) {
            scrambled.append((char) (character ^ XOR_KEY));
        }
        return scrambled.toString();
    }

    // Applies the same XOR operation — XOR is its own inverse, so this decodes obfuscated logs
    public static String decodeLog(String encodedMessage) {
        StringBuilder decoded = new StringBuilder();
        for (char character : encodedMessage.toCharArray()) {
            decoded.append((char) (character ^ XOR_KEY));
        }
        return decoded.toString();
    }
}
