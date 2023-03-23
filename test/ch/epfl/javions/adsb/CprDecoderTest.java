package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author @franklintra
 * @project Javions
 */
class CprDecoderTest {

    int[] long_CPR = new int[] {111600, 108865};
    int[] lat_CPR = new int[] {94445, 77558};

    double[] normalizedLong_CPR = new double[] {Math.scalb(long_CPR[0], -17), Math.scalb(long_CPR[1], -17)};
    double[] normalizedLat_CPR = new double[] {Math.scalb(lat_CPR[0], -17), Math.scalb(lat_CPR[1], -17)};

    @Test
    void throwExceptionIfMostRecentIsNot0Or1() {
        assertThrows(IllegalArgumentException.class, () -> CprDecoder.decodePosition(0, 0, 0, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> CprDecoder.decodePosition(0, 0, 0, 0, -1));
    }

    @Test
    void returnsNullIfLatitudeIsNotBetweenMinus90And90() {
        //this test is very complicated to make because we don't have examples of latitude and longitude that are not between -90 and 90 from local data
        assertNull(CprDecoder.decodePosition(0.01, 0.01, 0.03, 0.03, 0));
    }

    @Test
    void testDecodePositionWithGivenValues() {
        GeoPos[] expected = new GeoPos[]{new GeoPos((int) Units.convert(7.476062d, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(46.323349d, Units.Angle.DEGREE, Units.Angle.T32)), new GeoPos((int) Units.convert(7.475166d, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(46.322363d, Units.Angle.DEGREE, Units.Angle.T32))};
        double epsilon = 10e-9;
        GeoPos[] actual = new GeoPos[]{(CprDecoder.decodePosition(normalizedLong_CPR[0], normalizedLat_CPR[0], normalizedLong_CPR[1], normalizedLat_CPR[1], 0)),CprDecoder.decodePosition(normalizedLong_CPR[0], normalizedLat_CPR[0], normalizedLong_CPR[1], normalizedLat_CPR[1], 1)};
        for (GeoPos geoPos : actual) {
            assertNotNull(geoPos);
        }
        for (int i = 0; i < 2; i++) {
            assertEquals(expected[i].longitude(), actual[i].longitude(), epsilon);
            assertEquals(expected[i].latitude(), actual[i].latitude(), epsilon);
        }
        //GeoPos actualExternal = CprDecoder.decodePosition(Math.scalb(39846, 17), Math.scalb(92095, 17), Math.scalb(125818, 17), Math.scalb(88385, 17), 0);
    }
}
