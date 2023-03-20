package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftIdentificationMessageTest {
    String[] firstFiveIdMessagesExpected = new String[] {
            "AircraftIdentificationMessage[timeStampNs=1499146900, icaoAddress=IcaoAddress[string=4D2228], category=163, callSign=CallSign[string=RYR7JD]]\n",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, callSign=CallSign[string=MSC3361]]\n",
            "AircraftIdentificationMessage[timeStampNs=2698727800, icaoAddress=IcaoAddress[string=495299], category=163, callSign=CallSign[string=TAP931]]\n",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, callSign=CallSign[string=MSC3361]]\n",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, callSign=CallSign[string=MSC3361]]\n",
            "AircraftIdentificationMessage[timeStampNs=3215880100, icaoAddress=IcaoAddress[string=A4F239], category=165, callSign=CallSign[string=DAL153]]\n",
            "AircraftIdentificationMessage[timeStampNs=4103219900, icaoAddress=IcaoAddress[string=4B2964], category=161, callSign=CallSign[string=HBPRO]]"
    };

    @Test
    void testFirstFiveIdentificationMessages() throws IOException {
        AircraftIdentificationMessage A;
        int counter = 0;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null) {
                A = AircraftIdentificationMessage.of(m);
                if (counter < 5) {
                    System.out.println(A + "\n" + firstFiveIdMessagesExpected[counter]);
                    assertEquals(A.toString(), firstFiveIdMessagesExpected[counter]);
                }
                counter++;
            }
            assertNull(demodulator.nextMessage());
        }
    }
}
