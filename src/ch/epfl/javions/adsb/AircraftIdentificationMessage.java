package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    private static final int CHAR_LENGTH_ENCODED = 6;
    private static final int LETTERS_LOWER_BOUND = 1;
    public static final int LETTERS_UPPER_BOUND = 26;
    private static final int NUMBERS_LOWER_BOUND = 48;
    private static final int NUMBERS_UPPER_BOUND = 57;

    /**
     * Checks that the parameters are not null and that the time stamp is positive.
     *
     * @param timeStampNs the time stamp in nanoseconds
     * @param icaoAddress the ICAO description of the aircraft
     * @param category    the category of the aircraft
     * @param callSign    the call sign of the aircraft
     */
    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        if (callSign == null || icaoAddress == null) {
            throw new NullPointerException("One of the parameters is null");
        }
    }

    /**
     * Returns the AircraftIdentificationMessage corresponding to the given raw message if all the characters are valid, null otherwise.
     *
     * @param rawMessage the raw message
     * @return the corresponding AircraftIdentificationMessage
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        Preconditions.checkArgument(rawMessage.downLinkFormat() == 17);
        StringBuilder callSignString = new StringBuilder();
        //this loop goes from 42 to 0, 6 by 6, to ensure we decode the characters in order (and use the .append method with the String Builder).
        //the most significant bits contain the characters on the left
        for (int i = 42; i >= 0; i = i - CHAR_LENGTH_ENCODED) {
            Character ch = getChar(Bits.extractUInt(rawMessage.payload(), i, CHAR_LENGTH_ENCODED));
            if (ch == null) {
                return null;
            }
            callSignString.append(ch);
        }
        int category = (14 - (Bits.extractUInt(rawMessage.payload(), 51, 5)) << 4) + Bits.extractUInt(rawMessage.payload(), 48, 3);
        CallSign callSign = new CallSign(callSignString.toString().stripTrailing());
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }

    /**
     * Returns the character corresponding to the given integer as per ADS-B specification.
     * This method is optimized using character arithmetic instead of using a switch table.
     *
     * @param i the integer to convert to a character
     * @return the corresponding character or null if the integer is not valid according to the ADS-B specification
     */
    private static Character getChar(int i) {
        int SPACE_INTEGER = 32;
        if (LETTERS_LOWER_BOUND <= i && i <= LETTERS_UPPER_BOUND) {
            return (char) ('A' + i - 1);
        } else if (NUMBERS_LOWER_BOUND <= i && i <= NUMBERS_UPPER_BOUND) {
            return (char) ('0' + i - 48);
        } else if (i == SPACE_INTEGER) {
            return ' ';
        } else {
            return null;
        }
    }
}
