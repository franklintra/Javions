package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author @franklintra
 * @project Javions
 */
class MessageParserTest {
    private final IcaoAddress expected = new IcaoAddress("4D2228");
    private final GeoPos[] expectedPositions = new GeoPos[] {
            new GeoPos((int) Units.convert(5.620176717638969d, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71530147455633d, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.621292097494006, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.715926848351955, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.62225341796875, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71644593961537, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.623420681804419, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71704415604472, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.624397089704871, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71759032085538, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.625617997720838, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71820789948106, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.626741759479046, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.718826316297054, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.627952609211206, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.71946484968066, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.629119873046875, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.72007002308965, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.630081193521619, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.7205820735544, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.631163045763969, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.72120669297874, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.633909627795219, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.722671514377, Units.Angle.DEGREE, Units.Angle.T32)),
            new GeoPos((int) Units.convert(5.634819064289331, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(45.72314249351621, Units.Angle.DEGREE, Units.Angle.T32)),
    };

    @Test
    void testSpecificAircraft() throws IOException {
        List<double[]> values = Arrays.asList(new double[][]{
                {0, 0},
                {0, 0}
        });
        int counter = -1;
        try (var s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            var demodulator = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = demodulator.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expected)) continue;

                Message pm = MessageParser.parse(m);

                if (pm instanceof AirbornePositionMessage) {
                    values.set(((AirbornePositionMessage) pm).parity(), new double[]{((AirbornePositionMessage) pm).x(), ((AirbornePositionMessage) pm).y()});
                    GeoPos pos = CprDecoder.decodePosition(values.get(0)[0], values.get(0)[1], values.get(1)[0], values.get(1)[1], ((AirbornePositionMessage) pm).parity());
                    if (counter>=0) {
                        assertEquals(expectedPositions[counter], pos);
                    }
                    counter++;
                }
            }
        }
    }
}
