package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla
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
    //fixme : need as many useful MAGIC_NUMBERS as possible (private static final constants)

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
        //fixme : try and get rid of mult100GrayCode and mult500GrayCode and replace them with a cleaner solution
        int Q = Bits.extractUInt(rawMessage.payload(), 40, 1);
        double altitude = 0;
        //getting 12 bits from index 36 of the 56 bits, then masking to remove but from index 4 from the right of the 12 bits
        //within rawmessage index 1, we take out bytes 4 to 10, then extract 12 bites starting from index 36
        if (Q == 1) {
            long alt = Bits.extractUInt(rawMessage.payload(), 36, 12);
            long extractedBits = spliceOutBit(alt, 4);
            altitude = (extractedBits * 25) - 1000;
        } else if (Q == 0) {
            // Unscramble
            int[] sortedBits = unscramble(rawMessage);
            //separate into two groups, 3 bits from LSB, 9 bits from MSB
            int[] mult100GrayCode = new int[3];
            int[] mult500GrayCode = new int[9];
            System.arraycopy(sortedBits, 0, mult500GrayCode, 0, mult500GrayCode.length);
            System.arraycopy(sortedBits, 9, mult100GrayCode, 0, mult100GrayCode.length);
            // 0 5 6 are invalid
            if ((mult100GrayCode[0] == 0 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 0) ||
                    (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) ||
                    (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 1)) {
                return null;
            }
            // Swap 7 with 5
            if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 0) {
                mult100GrayCode[1] = 1;
                mult100GrayCode[2] = 1;
            }
            // convert to decimal
            int result500beforeSwaps = grayCodeToDecimal(mult500GrayCode);
            // Check if the value of result500beforeSwaps is 1, 3, 5 or 7   i.e. odd
            if (result500beforeSwaps % 2 == 1) {
                if (mult100GrayCode[0] == 0 && mult100GrayCode[1] == 0 && mult100GrayCode[2] == 1) { // 1 to 5
                    mult100GrayCode[0] = 1;
                    mult100GrayCode[1] = 1;
                } else if (mult100GrayCode[0] == 0 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) { // 2 to 4
                    mult100GrayCode[0] = 1;
                    mult100GrayCode[2] = 0;
                } else if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) { // 5 to 1 // fixme : this case is not reachable because there is an if return null above
                    mult100GrayCode[0] = 0;
                    mult100GrayCode[1] = 0;
                } else if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 0) { // 4 to 2
                    mult100GrayCode[0] = 0;
                    mult100GrayCode[2] = 1;
                }
            }
            // gray code to decimal
            int result100 = grayCodeToDecimal(mult100GrayCode);
            int result500 = grayCodeToDecimal(mult500GrayCode);
            altitude = -1300 + (result100 * 100) + (result500 * 500);
        }

        return new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                Units.convertFrom(altitude, Units.Length.FOOT),
                Bits.extractUInt(rawMessage.payload(), 34, 1),
                Bits.extractUInt(rawMessage.payload(), 0, 17) * Math.pow(2, -17),
                Bits.extractUInt(rawMessage.payload(), 17, 17) * Math.pow(2, -17)
        );
    }

    /**
     * This function removes the bit at index i from the long x
     *
     * @param x the long to remove the bit from
     * @param i the index of the bit to remove
     * @return the long with the bit removed
     */
    private static long spliceOutBit(long x, int i) { //fixme what is this for?
        long mask = ~(-1L << i);
        return (x & mask) | ((x >>> 1) & ~mask);
    }

    /**
     * Converts a gray code to decimal
     *
     * @param grayCode the gray code to convert
     * @return the decimal value of the gray code
     */
    private static int grayCodeToDecimal(int[] grayCode) { //fixme need a very precise documentation of input and output (is the array from left to right or right to left? and other)
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
        int[] sortedBits = new int[12];
        HashMap<Integer, Integer> sortingTable = new HashMap<>();
        int[] values = {9, 3, 10, 4, 11, 5, 6, 0, 7, 1, 8, 2}; // fixme: this needs to be a magic number or something
        for (int i = 0; i < values.length; i++) {
            sortingTable.put(47 - i, values[i]); // fixme: the 47 as well
        }
        for (Map.Entry<Integer, Integer> entry : sortingTable.entrySet()) {
            sortedBits[entry.getValue()] = Bits.extractUInt(rawMessage.payload(), entry.getKey(), 1);
        }
        return sortedBits;
    }
}