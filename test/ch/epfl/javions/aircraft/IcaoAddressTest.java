package ch.epfl.javions.aircraft;

import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;


public class IcaoAddressTest {


    @Test
    void testValidIcao() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("123456"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("ABDE"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("AAAAAA"));
    }

    @Test
    void testStringIcaoAddress() {
        IcaoAddress icao = new IcaoAddress("4B1814");
        assertEquals("4B1814", icao.string());
        assertEquals("ICAO address: 4B1814", icao.toString());
    }
}
