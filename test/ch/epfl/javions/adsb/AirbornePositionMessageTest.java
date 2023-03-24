package ch.epfl.javions.adsb;
import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
@SuppressWarnings("unused")
class AirbornePositionMessageTest {
    AirbornePositionMessage[] firstFiveIdMessagesExpected = new AirbornePositionMessage[] {
            new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 0.6867904663085938, 0.7254638671875),
            new AirbornePositionMessage(116538700, new IcaoAddress("4241A9"), 1303.02d, 0, 0.702667236328125, 0.7131423950195312),
            new AirbornePositionMessage(138560100, new IcaoAddress("4D2228"), 10972.800000000001d, 1, 0.6243515014648438, 0.4921417236328125),
            new AirbornePositionMessage(208135700, new IcaoAddress("4D029F"), 4244.34d, 0, 0.747222900390625, 0.7342300415039062),
            new AirbornePositionMessage(233069800, new IcaoAddress("3C6481"), 10370.82d, 0, 0.8674850463867188, 0.7413406372070312),
    };
    @Test
    void testWithInstructionSetValues() throws IOException {
        int counter = 0;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null && counter < 5) {
                int typeCode = Bits.extractUInt(m.payload(), 51, 5);
                if ((9 <= typeCode && typeCode <= 18) || (20 <= typeCode && typeCode <= 22)) {
                    assertEquals(firstFiveIdMessagesExpected[counter], AirbornePositionMessage.of(m));
                    counter++;
                }
            }
        }
    }
/*
    @Ignore
    public void mest() {
        int counter = 0;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null && counter < 5) {
                int typeCode = Bits.extractUInt(m.payload(), 51, 5);
                if ((9 <= typeCode && typeCode <= 18) || (20 <= typeCode && typeCode <= 22)) {
//                    System.out.println(AirbornePositionMessage.of(m));
                    System.out.println(m.bytes().toString());
                    counter++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(counter);
    }*/

    @Test
    void compactConstrusctorThrowsExceptionIfIcaoAddressIsNull() {
        assertThrows(NullPointerException.class, () ->  new AirbornePositionMessage(75898000, null, 10546.08d, 0, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    void compactConstrusctorThrowsExceptionIfTimeStampIsLessThanZero() {
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(-1, new IcaoAddress("495299"), 10546.08d, 0, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    void compactConstructorThrowsExceptionIfParityIsNotZeroOrOne() {
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 2, 0.6867904663085938, 0.7254638671875));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, -1, 0.6867904663085938, 0.7254638671875));
    }

    @Test
    void compactConstructorThrowsExceptionIfXOrYIsNotInRange() {
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 1.1, 0.7254638671875));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 0.6867904663085938, 1.1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, -0.1, 0.7254638671875));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 0.6867904663085938, -0.1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 0.6867904663085938, 1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 1, -0.1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () ->  new AirbornePositionMessage(75898000, new IcaoAddress("495299"), 10546.08d, 0, 1, 0));
    }
//    @Test
//    void testOfWithQIsZero() {
//        assertNull(AirbornePositionMessage.of(new RawMessage(1, new ByteString(HexFormat.of().parseHex("8D39203559B225F07550ADBE328F")))));
//    }
//    @Test
//    void testOfWithIfInvalidAltitude() {
//        assertNull(AirbornePositionMessage.of(new RawMessage(1, new ByteString(HexFormat.of().parseHex("8D392AE89B00009570AC00DDEBE5")))));
//    }

    @Test
    public void AltitudeComputerTestQis1(){    byte[] bytes = {(byte) 0x8D,(byte) 0x39,(byte) 0x20,(byte) 0x35,(byte) 0x59, (byte) 0xB2,(byte) 0x25, (byte) 0xF0,(byte) 0x75,(byte) 0x50, (byte) 0xAD, (byte) 0xBE,(byte) 0x32, (byte) 0x8F};
        AirbornePositionMessage expected = new AirbornePositionMessage(0, new IcaoAddress("392035"), 3474.72d, 1, 0.6575698852539062, 0.4848175048828125);
        assertEquals(expected.altitude(), AirbornePositionMessage.of(RawMessage.of(0, bytes)).altitude(), 10e-5);
    }
    @Test
    public void AnotherAltitudeComputerTestQis1(){    byte[] bytes = {(byte) 0x8D, (byte) 0xAE, 0x02, (byte) 0xC8, 0x58, 0x64, (byte) 0xA5, (byte) 0xF5, (byte) 0xDD, 0x49, 0x75, (byte) 0xA1, (byte) 0xA3, (byte) 0xF5};
        AirbornePositionMessage expected = new AirbornePositionMessage(0, new IcaoAddress("AE02C8"), 7315.20d, 1, 0.6867904663085938, 0.7254638671875);
        assertEquals(expected.altitude(), AirbornePositionMessage.of(new RawMessage(0, new ByteString(bytes))).altitude(), 10e-5);
    }
}