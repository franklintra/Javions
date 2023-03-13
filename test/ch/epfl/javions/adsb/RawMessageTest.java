package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @project Javions
 * @author @chukla
 */

public class RawMessageTest {

    @Test
    void ConstructorThrowsExceptionWhenTimeStampIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-1, new ByteString(new byte[14])));
    }
    @Test
    void ConstructorThrowsExceptionWhenBytesAreNot14() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(1, new ByteString(new byte[13])));
    }



}
