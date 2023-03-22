package ch.epfl.javions.adsb;/*

/**
 * @project Javions
 * @author @chukla
 */

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {


    public AirbornePositionMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(x < 0 || x >= 1 && y < 0 || y >= 1);
        if (icaoAddress == null) {
            throw new NullPointerException();
        }

    }

    AirbornePositionMessage of(RawMessage rawMessage) {
        int Q = Bits.extractUInt(rawMessage.payload(), 12, 1);

        //getting 12 bits from index 36 of the 56 bits, then masking to remove but from index 4 from the right of the 12 bits

        //within rawmessage index 1, we take out bytes 4 to 10, then extract 12 bites starting from index 36
        if (Q == 1) {

            int ALT = (int) ((rawMessage.payload() >> 36) & ~(1 << 4)) << 36;
            int altitude = -1000 + ALT * 25;
        } else if (Q == 0) {

            int[] C = new int[3], A = new int[3], B = new int[3], D = new int[3];
            int[] sortedBits = new int[12];

            A[0] = Bits.extractUInt(rawMessage.payload(), 18, 1);
            A[1] = Bits.extractUInt(rawMessage.payload(), 16, 1);
            A[2] = Bits.extractUInt(rawMessage.payload(), 14, 1);

            C[0] = Bits.extractUInt(rawMessage.payload(), 19, 1);
            C[1] = Bits.extractUInt(rawMessage.payload(), 17, 1);
            C[2] = Bits.extractUInt(rawMessage.payload(), 15, 1);

            B[0] = Bits.extractUInt(rawMessage.payload(), 13, 1);
            B[1] = Bits.extractUInt(rawMessage.payload(), 11, 1);
            B[2] = Bits.extractUInt(rawMessage.payload(), 9, 1);

            D[0] = Q;
            D[1] = Bits.extractUInt(rawMessage.payload(), 10, 1);
            D[2] = Bits.extractUInt(rawMessage.payload(), 8, 1);
            int i = 0;
            for (int k : D) {
                sortedBits[i++] = k;
            }
            for (int k : A) {
                sortedBits[i++] = k;
            }
            for (int k : B) {
                sortedBits[i++] = k;
            }
            for (int k : C) {
                sortedBits[i++] = k;
            }
        }

        return null; //to remove
    }


}
