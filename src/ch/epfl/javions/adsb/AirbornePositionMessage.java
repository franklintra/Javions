package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla (357550)
 */

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

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
    // This is the old position of the bits in order, starting from the right
    private static final int[] REORDERED_BIT_OLD_POSITIONS = {7, 9, 11, 1, 3, 5, 6, 8, 10, 0, 2, 4};
    //36 is the index of the last bit, starting from the right, of the altitude bits in the ME attribute
    private static final int ALT_INDEX_START = 36;
    // The bit position of the Q bit in the ME attribute, starting from the right
    private static final int Q_INDEX_POSITION = 40;
    // The bit position of the bit that indicates whether the message is even or odd, starting from the right
    private static final int PARITY_BIT = 34;
    // The bit position of the first bit of the longitude, starting from the right
    private static final int LONGITUDE_INDEX_START = 0;
    // The bit position of the first bit of the latitude, starting from the right
    private static final int LATITUDE_INDEX_START = 17;
    // The number of bits used to encode the longitude or latitude
    private static final int LONG_OR_LAT_BIT_LENGTH = 17;

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
            case 0 -> {
                // Unscramble
                int sortedBits = unscramble(rawMessage.payload());
                //separate into two groups, 3 bits from LSB, 9 bits from MSB
                int multipleOfHundredFoots = grayCodeToDecimal(sortedBits & 0b111);
                int multipleOfFiveHundredFoots = grayCodeToDecimal(sortedBits >> 3);

                // Check if multipleOfHundredFoots is invalid, making the altitude invalid
                if (multipleOfHundredFoots == 0 || multipleOfHundredFoots == 5 || multipleOfHundredFoots == 6) {
                    return null;
                }
                // If gray code is 7 in decimal, then change it to 5 in decimal
                if (multipleOfHundredFoots == 7) {
                    multipleOfHundredFoots = 5;
                }
                // Check if the value of multipleOfFiveHundredFoots is odd, and if so, change the value of multipleOfHundredFoots as per the specification
                if (multipleOfFiveHundredFoots % 2 == 1) {
                    multipleOfHundredFoots = 6 - multipleOfHundredFoots;
                }

                altitude = -1300 + (multipleOfHundredFoots * 100) + (multipleOfFiveHundredFoots * 500);
            }
            case 1 -> {
                // calculate the altitude directly from the bits
                altitude = getAltitudeForQ1(Bits.extractUInt(rawMessage.payload(), ALT_INDEX_START, NUM_ALT_BITS));
            }
        }
        return new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                Units.convertFrom(altitude, Units.Length.FOOT),
                Bits.extractUInt(rawMessage.payload(), PARITY_BIT, 1),
                Math.scalb(Bits.extractUInt(rawMessage.payload(), LONGITUDE_INDEX_START, LONG_OR_LAT_BIT_LENGTH), -17),
                Math.scalb(Bits.extractUInt(rawMessage.payload(), LATITUDE_INDEX_START, LONG_OR_LAT_BIT_LENGTH), -17)
        );
    }

    /**
     * This function removes the Q-bit that is on the 4th position from the right, starting from 0, of the altitude bits in the ME attribute
     * Then it converts the remaining 11 bits to decimal, and multiplies it by 25 to get the altitude in foots
     * Finally, it subtracts 1000 to get the altitude from the ground (as it's stored as the altitude from 1000 foots)
     * @param value the long to remove the bit from
     * @return the actual altitude in foots
     */
    private static long getAltitudeForQ1(long value) {
        final long mask = ~(-1L << 4);
        long alt =  (value & mask) | ((value >>> 1) & ~mask);
        return 25 * alt - 1000;
    }

    /**
     * Converts a gray code to decimal
     *
     * @param grayCode the gray code to convert
     * @return the decimal value of the gray code
     */
    private static int grayCodeToDecimal(int grayCode) {
        int decimal = 0;
        while (grayCode != 0) {
            decimal ^= grayCode;
            grayCode = grayCode >> 1;
        }
        return decimal;
    }

    /**
     * Unscrambles the bits in the payload as per the ADS-B standard
     *
     * @param payload the long of the raw message to unscramble
     * @return the unscrambled bits
     */
    private static int unscramble(long payload) {
        int sortedBits = 0;
        int rawBits = Bits.extractUInt(payload, ALT_INDEX_START, NUM_ALT_BITS);
        for (int i = 0; i < NUM_ALT_BITS; i++) {
            int valueToPut = Bits.testBit(rawBits, REORDERED_BIT_OLD_POSITIONS[i]) ? 1 : 0;
            sortedBits |= (valueToPut << i);
        }
        return sortedBits;
    }
}