package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla
 */

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {

    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(x < 1);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(y < 1);

        if (icaoAddress == null) {
            throw new NullPointerException();
        }

    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {
        int Q = Bits.extractUInt(rawMessage.payload(), 40, 1);

        double altitude = 0;

        //getting 12 bits from index 36 of the 56 bits, then masking to remove but from index 4 from the right of the 12 bits

        //within rawmessage index 1, we take out bytes 4 to 10, then extract 12 bites starting from index 36
        if (Q == 1) {


            long alt = Bits.extractUInt(rawMessage.payload(), 36, 12);


            long extractedBits = spliceOutBit(alt, 4);
            altitude = (extractedBits * 25) - 1000;


        } else if (Q == 0) {

            System.out.println("yalla");
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
                System.out.println("Invalid value for mult100GrayCode");
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
                } else if (mult100GrayCode[0] == 1 && mult100GrayCode[1] == 1 && mult100GrayCode[2] == 1) { // 5 to 1
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

            altitude = -1300 + (result100 * 100) + (result500* 500);


        }

        return new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                Units.convertFrom(altitude, Units.Length.FOOT),
                Bits.extractUInt(rawMessage.payload(),34 , 1),
                Bits.extractUInt(rawMessage.payload(),0,17) * Math.pow(2,-17),
                Bits.extractUInt(rawMessage.payload(),17,17) * Math.pow(2,-17)
        );
    }

    private static long spliceOutBit(long x, int i) {
        long mask = ~(-1L << i);
        return (x & mask) | ((x >>> 1) & ~mask);
    }

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

    private static int[] unscramble(RawMessage rawMessage) {
        int[] sortedBits = new int[12];

        HashMap<Integer, Integer> sortingTable = new HashMap<>();

        int[] values = {9, 3, 10, 4, 11, 5, 6, 0, 7, 1, 8, 2};
        for (int i = 0; i < values.length; i++) {
            sortingTable.put(47 - i, values[i]);
        }

        for (Map.Entry<Integer, Integer> entry : sortingTable.entrySet()) {
            sortedBits[entry.getValue()] = Bits.extractUInt(rawMessage.payload(), entry.getKey(), 1);
        }

        return sortedBits;
    }

//    public static void main(String[] args) {
//        long l = 0b1000_0000_0000_0101_0100_0000_0000_0000_0000_0000_0000_0000_0000_0000L;
//
//        System.out.println(Long.toBinaryString(l));
//        System.out.println(Bits.extractUInt(l, 40, 1));
//        l = spliceOutBit(l, 40);
//
//        System.out.println(Long.toBinaryString(l));
//    }

}

