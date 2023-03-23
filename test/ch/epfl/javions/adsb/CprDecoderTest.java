package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author @franklintra
 * @project Javions
 */
class CprDecoderTest {
    @Test
    void throwExceptionIfMostRecentIsNot0Or1() {
        assertThrows(IllegalArgumentException.class, () -> CprDecoder.decodePosition(0, 0, 0, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> CprDecoder.decodePosition(0, 0, 0, 0, -1));
    }

    @Test
    void returnsNullIfLatitudeIsNotBetweenMinus90And90() {
        //todo implement
    }

    @Test
    void testDecodePosition() {
        //todo implement properly
        System.out.println(CprDecoder.decodePosition(111600*Math.pow(2, -17), 94445*Math.pow(2, -17), 108865*Math.pow(2, -17), 77558*Math.pow(2, -17), 0));
        System.out.println(CprDecoder.decodePosition(111600*Math.pow(2, -17), 94445*Math.pow(2, -17), 108865*Math.pow(2, -17), 77558*Math.pow(2, -17), 1));

    }
}
