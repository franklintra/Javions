package ch.epfl.javions.adsb;
import ch.epfl.javions.Bits;
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
            new AirbornePositionMessage(233069800, new IcaoAddress("3C6481"), 10370.82d, 0, 0.8674850463867188, 0.7413406372070312)
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
                    assertEquals(firstFiveIdMessagesExpected[counter], AircraftIdentificationMessage.of(m));
                    counter++;
                }
            }
        }
        System.out.println(counter);
    }
}