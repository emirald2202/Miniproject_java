



package priority;

import enums.Status;

public class PriorityCalculator {

    
    public static final int TYPE_INFRASTRUCTURE = 1;
    public static final int TYPE_CORRUPTION     = 2;
    public static final int TYPE_NOISE          = 3;
    public static final int TYPE_TRAFFIC        = 4;
    public static final int TYPE_SANITATION     = 5;
    public static final int TYPE_WATER_SUPPLY   = 6;
    public static final int TYPE_ELECTRICITY    = 7;

    
    private static final int XOR_KEY = 0b10101010;

    
    
    
    public static int calculateScore(int complaintType, int urgencyLevel, int areaCode) {
        return (complaintType << 2) + urgencyLevel;
    }

    
    public static Status autoAssignStatus(int finalScore) {
        if (finalScore > 20) {
            return Status.ESCALATED;
        } else if (finalScore > 12) {
            return Status.UNDER_REVIEW;
        } else {
            return Status.FILED;
        }
    }

    
    public static String obfuscateLog(String message) {
        StringBuilder scrambled = new StringBuilder();
        for (char character : message.toCharArray()) {
            scrambled.append((char) (character ^ XOR_KEY));
        }
        return scrambled.toString();
    }

    
    public static String decodeLog(String encodedMessage) {
        StringBuilder decoded = new StringBuilder();
        for (char character : encodedMessage.toCharArray()) {
            decoded.append((char) (character ^ XOR_KEY));
        }
        return decoded.toString();
    }
}
