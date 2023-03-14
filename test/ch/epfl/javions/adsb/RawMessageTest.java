package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void ofReturnsNullIfCRCNotZero() { // TODO: 3/14/2023 doesnt work
        byte[] bytes = new byte[14];
        Crc24 crc = new Crc24(Crc24.GENERATOR);

        if (crc.crc(bytes) != 0) {
            assertThrows(null, () -> RawMessage.of(1, bytes));
        }

    }


}
