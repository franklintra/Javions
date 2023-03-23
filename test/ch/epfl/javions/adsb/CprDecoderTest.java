package ch.epfl.javions.adsb;

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
        //GeoPos actualExternal = CprDecoder.decodePosition(0.3919, 0.7095, 0.3829,0.5658, 0);
    }

    @Test
    void testWithEdStemTeacherValues() {
        double x0 = Math.scalb(111600d, -17);
        double y0 = Math.scalb(94445d, -17);
        double x1 = Math.scalb(108865d, -17);
        double y1 = Math.scalb(77558d, -17);
        GeoPos p = CprDecoder.decodePosition(x0, y0, x1, y1, 0);
        assertEquals(new GeoPos(89192898, 552659081), p);
    }

    @Test
    void testWithRandomValues() {
        GeoPos even = CprDecoder.decodePosition(0.3,0.3,0.3,0.3,0);
        GeoPos odd = CprDecoder.decodePosition(0.3, 0.3, 0.3, 0.3, 1);
        assertNotNull(even);
        assertNotNull(odd);
        assertEquals(1.8305084947496653d, Units.convertTo(even.longitude(), Units.Angle.DEGREE), 10e-9);
        assertEquals(1.7999999597668648d, Units.convertTo(even.latitude(), Units.Angle.DEGREE), 10e-9);
        assertEquals(1.862068958580494d, Units.convertTo(odd.longitude(), Units.Angle.DEGREE), 10e-9);
        assertEquals(1.8305084947496653d, Units.convertTo(odd.latitude(), Units.Angle.DEGREE), 10e-9);
    }

    @Test
    void testWithInternetValues() {
        //got these values from : http://airmetar.main.jp/radio/ADS-B%20Decoding%20Guide.pdf
        GeoPos even = CprDecoder.decodePosition(0.3919, 0.7095, 0.3829, 0.5658, 0);
        GeoPos odd = CprDecoder.decodePosition(0.3919, 0.7095, 0.3829, 0.5658, 1);
        assertNotNull(even);
        assertNotNull(odd);
        assertEquals(Units.convertTo(even.latitude(), Units.Angle.DEGREE), 52.25720214843750d, 10e-4);
        assertEquals(Units.convertTo(odd.latitude(), Units.Angle.DEGREE), 52.26578017412606d, 10e-4);
        assertEquals(Units.convertTo(even.longitude(), Units.Angle.DEGREE), 3.91937d, 10e-4);
        assertEquals(Units.convertTo(odd.longitude(), Units.Angle.DEGREE), 3.91937d, 10e-1);
    }
}