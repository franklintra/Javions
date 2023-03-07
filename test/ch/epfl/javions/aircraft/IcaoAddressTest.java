package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SuppressWarnings("unused")
class IcaoAddressTest {
    @Test
    void testValidIcao() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("ABDE"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("AB-F3G"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("Ab12FG"));
    }

    @Test
    void testStringIcaoAddress() {
        IcaoAddress icao = new IcaoAddress("4B1814");
        assertEquals("4B1814", icao.string());
        assertEquals("ICAO address: 4B1814", icao.toString());
    }
}
