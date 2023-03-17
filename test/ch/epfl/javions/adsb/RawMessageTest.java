package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

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
    void ofReturnsNullIfCRCNotZero() {
        byte[] bytes = new byte[14];
        bytes[12] = (byte) "ooga booga".toCharArray()[0];
        assertNull(RawMessage.of(1, bytes));
    }

    @Test
    void ofReturnsCorrectMessage() { // FIXME: 3/15/2023 fix this later
       byte[] bytes = new byte[14];
        RawMessage message = RawMessage.of(1, bytes);
        assert message != null;
        assertEquals(1L, message.timeStampNs());
        assertEquals(new ByteString(bytes), message.bytes());
    }

    @Test
    void methodSizeGivesCorrectSize() { // FIXME: 3/15/2023 test should be fine but check
        byte[] bytes = new byte[14];
        bytes[0] = (byte) 0b10001000;
        assertEquals(14, RawMessage.size(bytes[0]));
        bytes[0] = (byte) 0b00000000;
        assertEquals(0, RawMessage.size(bytes[0]));
    }

    @Test
    void testTypeCodeReturnsTypeCode() { // FIXME: 3/15/2023 test should be fine but check
        assertEquals(0b00100 , RawMessage.typeCode(0b00100000_001011001100001101110001110000110010110011100000L));
        assertEquals(0b01011 , RawMessage.typeCode(0b01011000_110000111000001011010110100101000111000100000010L));
    }

    @Test
    void testDownlinkFormatReturnsDownlinkFormat() { // FIXME: 3/15/2023 test should be fine but check
        byte[] bytes = new byte[14];
        bytes[0] = (byte) 0b10001000;
        RawMessage message = new RawMessage(1, new ByteString(bytes));
        assertEquals( 0b10001, message.downLinkFormat());
    }

    @Test
    void testIcaoAddressReturnsIcaoAddress() {

        String msgHex = "8D" + "ABCDEF" + "994409940838175B284F";
        byte[] bytes = HexFormat.of().withUpperCase().withLowerCase().parseHex(msgHex);


        RawMessage message = new RawMessage(1, new ByteString(bytes));
        assertEquals("ICAO address: ABCDEF", message.icaoAddress().toString());
    }
}
