package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftIdentificationMessageTest {
    String[] firstFiveIdMessagesExpected = new String[] {
            "AircraftIdentificationMessage[timeStampNs=1499146900, icaoAddress=IcaoAddress[string=4D2228], category=163, string=CallSign[string=RYR7JD]]",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, string=CallSign[string=MSC3361]]",
            "AircraftIdentificationMessage[timeStampNs=2698727800, icaoAddress=IcaoAddress[string=495299], category=163, string=CallSign[string=TAP931]]",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, string=CallSign[string=MSC3361]]",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, string=CallSign[string=MSC3361]]",
            "AircraftIdentificationMessage[timeStampNs=3215880100, icaoAddress=IcaoAddress[string=A4F239], category=165, callSign=CallSign[string=DAL153]]",
            "AircraftIdentificationMessage[ timeStampNs=4103219900, icaoAddress=IcaoAddress[string=4B2964], category=161, callSign=CallSign[string=HBPRO]]"
    };

    @Test
    void testFirstFiveIdentificationMessages() throws IOException {
        AircraftIdentificationMessage A;
        int counter = 0;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null) {
                if (counter < 5 && (Arrays.asList(1, 2, 3, 4).contains((Bits.extractUInt(m.payload(),51,5))))) {
                    A = AircraftIdentificationMessage.of(m);
                    System.out.println("expected : " + A + "\n" + "actual   : " + firstFiveIdMessagesExpected[counter]);
                    assertEquals(firstFiveIdMessagesExpected[counter], A.toString());
                    counter++;
                }
            }
            assertNull(demodulator.nextMessage());
        }
    }
}
