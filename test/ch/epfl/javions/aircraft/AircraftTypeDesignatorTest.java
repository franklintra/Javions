package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTypeDesignatorTest {
    @Test
    void testValidTypeDesignator() {
        // Valid type designators
        AircraftTypeDesignator atd1 = new AircraftTypeDesignator("A20N");
        assertEquals("A20N", atd1.string());
        AircraftTypeDesignator atd2 = new AircraftTypeDesignator("A412");
        assertEquals("A412", atd2.string());
        AircraftTypeDesignator atd3 = new AircraftTypeDesignator("AB12");
        assertEquals("AB12", atd3.string());
        AircraftTypeDesignator atd4 = new AircraftTypeDesignator("KF98");
        assertEquals("KF98", atd4.string());
    }

    @Test
    void testInvalidTypeDesignator() {
        // Invalid type designators
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator(",###K"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("127"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("ABDEFG"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("A"));
    }

    @Test
    void testEmptyTypeDesignator() {
        // Empty type designator is allowed
        AircraftTypeDesignator atd = new AircraftTypeDesignator("");
        assertNull(atd.string());
    }
}