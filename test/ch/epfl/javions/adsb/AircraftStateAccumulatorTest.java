package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


class AircraftStateAccumulatorTest {
    @Test
    void main() throws IOException {
        String f = "samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a = new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    /**
     * Tests that the constructor throws a NullPointerException when the state setter is null.
     */
    @Test
    void constructorThrowsExceptionWhenZero() {
        assertThrows(NullPointerException.class, () -> new AircraftStateAccumulator(null));
    }

}