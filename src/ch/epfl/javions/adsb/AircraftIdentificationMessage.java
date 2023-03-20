package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra
 * @project Javions
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) {
    /**
     * Checks that the parameters are not null and that the time stamp is positive.
     * @param timeStampNs the time stamp in nanoseconds
     * @param icaoAddress the ICAO string of the aircraft
     * @param category the category of the aircraft
     * @param callSign the call sign of the aircraft
     */
    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        if (callSign == null || icaoAddress == null) {
            throw new NullPointerException("One of the parameters is null");
        }
    }

    private static char getChar(int i) {
        // return the corresponding letter if 1 <= i <= 26, return the corresponding number if 48 <= i <= 57 and a space if i == 32
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (i >= 1 && i <= 26) {
            return letters[i - 1].charAt(0);
        } else if (i >= 48 && i <= 57) {
            return numbers[i - 48].charAt(0);
        } else if (i == 32) {
            return ' ';
        } else {
            throw new IllegalArgumentException("The integer is not in the range of our custom characters table");
        }
    }

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        //fixme : check how extractUInt works exactly (is start from the left or the right? ) and does it decode right to left or left to right?
        Preconditions.checkArgument(rawMessage.downLinkFormat() == 17);
        int category = (14 - (Bits.extractUInt(rawMessage.payload(),51,5))<<4) + Bits.extractUInt(rawMessage.payload(), 48, 3);
        StringBuilder callSignString = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            callSignString.insert(0, getChar(Bits.extractUInt(rawMessage.payload(), i * 6, 6)));
        }
        CallSign callSign = new CallSign(callSignString.toString().stripTrailing());
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }
}
