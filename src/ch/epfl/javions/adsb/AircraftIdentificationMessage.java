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
     * @param icaoAddress the ICAO description of the aircraft
     * @param category the category of the aircraft
     * @param callSign the call sign of the aircraft
     */
    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        if (callSign == null || icaoAddress == null) {
            throw new NullPointerException("One of the parameters is null");
        }
    }

    /**
     * Returns the AircraftIdentificationMessage corresponding to the given raw message if all the characters are valid, null otherwise.
     * @param rawMessage the raw message
     * @return the corresponding AircraftIdentificationMessage
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        Preconditions.checkArgument(rawMessage.downLinkFormat() == 17);
        int category = (14 - (Bits.extractUInt(rawMessage.payload(), 51, 5)) << 4) + Bits.extractUInt(rawMessage.payload(), 48, 3);
        StringBuilder callSignString = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            char ch = getChar(Bits.extractUInt(rawMessage.payload(), i * 6, 6));
            if (ch == '\uFFFD') {
                return null;
            }
            callSignString.insert(0, ch);
        }
        CallSign callSign = new CallSign(callSignString.toString().stripTrailing());
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }

    /**
     * Returns the character corresponding to the given integer as per ADS-B specification.
     * This method is optimized using character arithmetic instead of using a table.
     * @param i the integer to convert to a character
     * @return the corresponding character or '\uFFFD' if the integer is not valid according to the ADS-B specification
     */
    private static char getChar(int i) {
        if (i >= 1 && i <= 26) {
            return (char) ('A' + i - 1);
        } else if (i >= 48 && i <= 57) {
            return (char) ('0' + i - 48);
        } else if (i == 32) {
            return ' ';
        } else {
            return '\uFFFD'; // '\uFFFD' for error handling (invalid character)
        }
    }
}
