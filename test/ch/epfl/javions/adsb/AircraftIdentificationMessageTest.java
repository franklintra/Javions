package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
class AircraftIdentificationMessageTest {
    AircraftIdentificationMessage[] firstFiveIdMessagesExpected_ = new AircraftIdentificationMessage[] {
            new AircraftIdentificationMessage(1499146900L, new IcaoAddress("4D2228"), 163, new CallSign("RYR7JD")),
            new AircraftIdentificationMessage(2240535600L, new IcaoAddress("01024C"), 163, new CallSign("MSC3361")),
            new AircraftIdentificationMessage(2698727800L, new IcaoAddress("495299"), 163, new CallSign("TAP931")),
            new AircraftIdentificationMessage(3215880100L, new IcaoAddress("A4F239"), 165, new CallSign("DAL153")),
            new AircraftIdentificationMessage(4103219900L, new IcaoAddress("4B2964"), 161, new CallSign("HBPRO"))
    };
    String[] firstFiveIdMessagesExpected = new String[] {
            "AircraftIdentificationMessage[timeStampNs=1499146900, icaoAddress=IcaoAddress[string=4D2228], category=163, callSign=CallSign[string=RYR7JD]]",
            "AircraftIdentificationMessage[timeStampNs=2240535600, icaoAddress=IcaoAddress[string=01024C], category=163, callSign=CallSign[string=MSC3361]]",
            "AircraftIdentificationMessage[timeStampNs=2698727800, icaoAddress=IcaoAddress[string=495299], category=163, callSign=CallSign[string=TAP931]]",
            "AircraftIdentificationMessage[timeStampNs=3215880100, icaoAddress=IcaoAddress[string=A4F239], category=165, callSign=CallSign[string=DAL153]]",
            "AircraftIdentificationMessage[timeStampNs=4103219900, icaoAddress=IcaoAddress[string=4B2964], category=161, callSign=CallSign[string=HBPRO]]"
    };

    @Test
    void testFirstFiveIdentificationMessagesWithOf() throws IOException {
        int counter = 0;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null && counter < 5) {
                if (AircraftIdentificationMessage.of(m) != null) { // if the message is an identification message
                    //System.out.println("expected : " + firstFiveIdMessagesExpected[counter] + "\n" + "actual   : " + A);
                    assertEquals(firstFiveIdMessagesExpected[counter], AircraftIdentificationMessage.of(m).toString());
                    assertEquals(firstFiveIdMessagesExpected_[counter], AircraftIdentificationMessage.of(m));
                    counter++;
                }
            }
        }
    }

    @Test
    void testConstructorNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(0, null, 0, new CallSign("HBPRO")));
        assertThrows(NullPointerException.class, () -> new AircraftIdentificationMessage(0, new IcaoAddress("4B1814"), 0, null));
    }

    @Test
    void testConstructorIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(-1, new IcaoAddress("4B1814"), 0, new CallSign("HBPRO")));
        assertThrows(IllegalArgumentException.class, () -> new AircraftIdentificationMessage(0, new IcaoAddress("az"), 0, new CallSign("HBPRO")));
    }

    @Test
    void testOfMethodReturnsNull() {
        //todo: test with a message that is not an identification message but very close to it for example
    }
}
