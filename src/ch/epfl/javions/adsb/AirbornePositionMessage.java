package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla (357550)
 */

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an ADS-B airborne position message.
 * This is a record to avoid boilerplate code.
 *
 * @param timeStampNs the time stamp of the message in nanoseconds
 * @param icaoAddress the ICAO address of the aircraft
 * @param altitude    the altitude of the aircraft in meters
 * @param parity      the parity of the message
 * @param x           the x coordinate of the aircraft in the ADS-B reference frame
 * @param y           the y coordinate of the aircraft in the ADS-B reference frame
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity,
                                      double x, double y) implements Message {
    // Number of bits used to encode the altitude
    private static final int NUM_ALT_BITS = 12;
    // The bit new bit positions of the altitude bits after the reordering
    private static final int[] REORDERED_BIT_POSITIONS = {9, 3, 10, 4, 11, 5, 6, 0, 7, 1, 8, 2};
    // 36+12+1 = 47 is the index of the first bit, starting from the right, of the altitude bits in the ME attribute
    private static final int ALT_INDEX_END = 47;
    //36 is the index of the last bit, starting from the right, of the altitude bits in the ME attribute
    private static final int ALT_INDEX_START = 36;
    // The bit position of the Q bit in the ME attribute, starting from the right
    private static final int Q_INDEX_POSITION = 40;

    private static final int LONGITUDE_INDEX_START = 0;
    private static final int LONGITUDE_BIT_LENGTH = 17;
    private static final int LATITUDE_INDEX_START = 17;
    private static final int LATITUDE_BIT_LENGTH = 17;

    /**
     * Checks that all the arguments given are valid.
     */
    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(x >= 0 && x < 1 && y >= 0 && y < 1);
        if (icaoAddress == null) {
            throw new NullPointerException();
        }
    }

    /**
     * If the raw message can be decoded as an airborne position message, returns the corresponding message as an object.
     * Otherwise, returns null.
     *
     * @param rawMessage the raw message to decode
     * @return the decoded message, or null if the message cannot be decoded
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        int Q = Bits.extractUInt(rawMessage.payload(), Q_INDEX_POSITION, 1);
        double altitude = 0;

        switch (Q) {
            case 1 -> {
                // extracts 12 bits that represent the altitude in the ME attribute and masks the 4th bit from the right (starting from 0)
                long alt = Bits.extractUInt(rawMessage.payload(), ALT_INDEX_START, NUM_ALT_BITS);
                long extractedBits = spliceOutFourthBit(alt);
                altitude = (extractedBits * 25) - 1000;
            }
            case 0 -> {
                // Unscramble
                int[] sortedBits = unscramble(rawMessage);
                //separate into two groups, 3 bits from LSB, 9 bits from MSB
                int[] mult100GrayCode = new int[3];
                int[] mult500GrayCode = new int[9];
                System.arraycopy(sortedBits, 0, mult500GrayCode, 0, mult500GrayCode.length);
                System.arraycopy(sortedBits, 9, mult100GrayCode, 0, mult100GrayCode.length);
                // Check if mult100GrayCode is invalid
                if (checkInvalidityGrayCode(mult100GrayCode)) {
                    return null;
                }
                // If gray code is 7 in decimal, then change it to 5 in decimal
                if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 0) {
                    mult100GrayCode[1] = 1;
                    mult100GrayCode[2] = 1;
                }
                // convert gray code to decimal
                int result500beforeSwaps = grayCodeToDecimal(mult500GrayCode);
                // Check if the value of result500beforeSwaps is 1, 3, 5 or 7   i.e. odd then mirror the gray code
                if (result500beforeSwaps % 2 == 1) {
                    changeMult100GrayCode(mult100GrayCode);
                }
                // convert gray code to decimal
                int result100 = grayCodeToDecimal(mult100GrayCode);
                int result500 = grayCodeToDecimal(mult500GrayCode);
                altitude = -1300 + (result100 * 100) + (result500 * 500);
            }
        }
        return new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                Units.convertFrom(altitude, Units.Length.FOOT),
                Bits.extractUInt(rawMessage.payload(), 34, 1),
                Bits.extractUInt(rawMessage.payload(), LONGITUDE_INDEX_START, LONGITUDE_BIT_LENGTH) * Math.pow(2, -17),
                Bits.extractUInt(rawMessage.payload(), LATITUDE_INDEX_START, LATITUDE_BIT_LENGTH) * Math.pow(2, -17)
        );
    }

    /**
     * Mirrors the given 3-bit gray code of a multiple of 100
     *
     * @param mult100GrayCode an integer array representing the 3-bit gray code of a multiple of 100
     */
    private static void changeMult100GrayCode(int[] mult100GrayCode) {
        // mirrors the gray code by interpreting it in its decimal values
        if (mult100GrayCode[0] == 0 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 1) { // 1 mirrored to 5
            mult100GrayCode[0] = 1;
            mult100GrayCode[1] = 1;
        } else if (mult100GrayCode[0] == 0 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) { // 2 mirrored to 4
            mult100GrayCode[0] = 1;
            mult100GrayCode[2] = 0;
        } else if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) { // 5 mirrored to 1
            mult100GrayCode[0] = 0;
            mult100GrayCode[1] = 0;
        } else if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 0) { // 4 mirrored to 2
            mult100GrayCode[0] = 0;
            mult100GrayCode[2] = 1;
        }
    }

    /**
     * Checks if the given 3-bit gray code of a multiple of 100 in decimal is invalid.
     * An invalid gray code is one that represents 0, 5, or 6 in decimal.
     *
     * @param mult100GrayCode an integer array representing the 3-bit gray code of a multiple of 100 in decimal
     * @return true if the given gray code is invalid, false otherwise
     */
    private static boolean checkInvalidityGrayCode(int[] mult100GrayCode) {
        // decimal values 0, 5, and 6 of the gray code are invalid
        return (mult100GrayCode[0] == 0 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 0) ||
                (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) ||
                (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 1);
    }

    /**
     * This function removes the 4th bit from the right, starting from 0, of the altitude bits in the ME attribute
     *
     * @param x the long to remove the bit from
     * @return the long with the bit removed
     */
    private static long spliceOutFourthBit(long x) {
        final long mask = ~(-1L << 4);
        return (x & mask) | ((x >>> 1) & ~mask);
    }

    /**
     * Converts a gray code to decimal
     *
     * @param grayCode the gray code to convert
     * @return the decimal value of the gray code
     */
    private static int grayCodeToDecimal(int[] grayCode) {
        int result = 0;
        for (int i = 0; i < grayCode.length; i++) {
            if (i == 0) {
                for (int j = 0; j < grayCode.length; j++) {
                    result += grayCode[j] * Math.pow(2, grayCode.length - j - 1);
                }
            } else {
                int dec = 0;
                for (int j = 0; j < grayCode.length; j++) {
                    dec += grayCode[j] * Math.pow(2, grayCode.length - j - 1);
                }
                result = result ^ (dec >> i);
            }
        }
        return result;
    }

    /**
     * Unscrambles the bits in the payload as per the ADS-B standard
     *
     * @param rawMessage the raw message to unscramble
     * @return the unscrambled bits
     */
    private static int[] unscramble(RawMessage rawMessage) {
        int[] sortedBits = new int[NUM_ALT_BITS];
        HashMap<Integer, Integer> sortingTable = new HashMap<>();
        int[] values = REORDERED_BIT_POSITIONS;
        for (int i = 0; i < values.length; i++) {
            sortingTable.put(ALT_INDEX_END - i, values[i]);
        }
        for (Map.Entry<Integer, Integer> entry : sortingTable.entrySet()) {
            sortedBits[entry.getValue()] = Bits.extractUInt(rawMessage.payload(), entry.getKey(), 1);
        }
        return sortedBits;
    }
}