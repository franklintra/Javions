package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla
 */

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HashMap;
import java.util.Map;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {

    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(x >= 0 || x < 1 && y >= 0 || y < 1);
        if (icaoAddress == null) {
            throw new NullPointerException();
        }

    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {
        int Q = Bits.extractUInt(rawMessage.payload(), 12, 1);


        double altitude = 0;

        //getting 12 bits from index 36 of the 56 bits, then masking to remove but from index 4 from the right of the 12 bits

        //within rawmessage index 1, we take out bytes 4 to 10, then extract 12 bites starting from index 36
        if (Q == 1) {

            int ALT = (int) ((rawMessage.payload() >> 36) & ~(1 << 4)) << 36; // FIXME: 3/23/2023 check if this mask is correct
            altitude = -1000 + ALT * 25;
        } else if (Q == 0) {
            int[] sortedBits = new int[12];

            int[] mult100GrayCode = new int[3];
            int[] mult500GrayCode = new int[9];

            HashMap<Integer, Integer> sortingTable = new HashMap<>();

            int[] values = {9, 3, 10, 4, 11, 5, 6, 0, 7, 1, 8, 2};
            for (int i = 0; i < values.length; i++) {
                sortingTable.put(19 - i, values[i]);
            }

            for (Map.Entry<Integer, Integer> entry : sortingTable.entrySet()) {
                sortedBits[entry.getValue()] = Bits.extractUInt(rawMessage.payload(), entry.getKey(), 1);
            }

            //separate into two groups, 3 bits from LSB, 9 bits from MSB
            System.arraycopy(sortedBits, 0, mult500GrayCode, 0, mult500GrayCode.length);
            System.arraycopy(sortedBits, 9, mult100GrayCode, 0, mult100GrayCode.length);


            // transformation

            // 0 5 6
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


            int result500beforeSwaps = 0;

            for (int i = 0; i < mult500GrayCode.length; i++) {
                if (i == 0) {

                    for (int j = 0; j < mult500GrayCode.length; j++) {
                        result500beforeSwaps += mult500GrayCode[j] * Math.pow(2, mult500GrayCode.length - j - 1);
                    }

                } else {

                    int dec = 0;

                    for (int j = 0; j < mult500GrayCode.length; j++) {
                        dec += mult500GrayCode[j] * Math.pow(2, mult500GrayCode.length - j - 1);
                    }

                    result500beforeSwaps = result500beforeSwaps ^ (dec >> i);
                }
            }


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
            int result100 = 0;

            for (int i = 0; i < mult100GrayCode.length; i++) {
                if (i == 0) {

                    for (int j = 0; j < mult100GrayCode.length; j++) {
                        result100 += mult100GrayCode[j] * Math.pow(2, mult100GrayCode.length - j - 1);
                    }

                } else {

                    int dec = 0;

                    for (int j = 0; j < mult100GrayCode.length; j++) {
                        dec += mult100GrayCode[j] * Math.pow(2, mult100GrayCode.length - j - 1);
                    }

                    result100 = result100 ^ (dec >> i);
                }
            }


            int result500 = 0;

            for (int i = 0; i < mult500GrayCode.length; i++) {
                if (i == 0) {

                    for (int j = 0; j < mult500GrayCode.length; j++) {
                        result500 += mult500GrayCode[j] * Math.pow(2, mult500GrayCode.length - j - 1);
                    }

                } else {

                    int dec = 0;

                    for (int j = 0; j < mult500GrayCode.length; j++) {
                        dec += mult500GrayCode[j] * Math.pow(2, mult500GrayCode.length - j - 1);
                    }

                    result500 = result500 ^ (dec >> i);
                }
            }


            altitude = -1300 + (result100 * 100) + (result500beforeSwaps * 500);


        }

        return new AirbornePositionMessage(
                rawMessage.timeStampNs(),
                rawMessage.icaoAddress(),
                altitude,
                Bits.extractUInt(rawMessage.payload(), 21, 1), // TODO: 3/23/2023 check if this is correct
                Bits.extractUInt(rawMessage.payload(), 38, 17),
                Bits.extractUInt(rawMessage.payload(), 55, 17)
        );
    }
}

