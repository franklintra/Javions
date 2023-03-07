package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unused")
class AircraftTypeDesignatorTest {
    @Test
    void testValidTypeDesignator() {
        // Valid type designators
        AircraftTypeDesignator atd1 = new AircraftTypeDesignator("A20N");
        assertEquals("A20N", atd1.string());
        AircraftTypeDesignator atd2 = new AircraftTypeDesignator("A412");
        assertEquals("A412", atd2.string());
        AircraftTypeDesignator atd3 = new AircraftTypeDesignator("A6");
        assertEquals("A6", atd3.string());
        AircraftTypeDesignator atd4 = new AircraftTypeDesignator("7B4");
        assertEquals("7B4", atd4.string());
    }

    @Test
    void testInvalidTypeDesignator() {
        // Invalid type designators
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("A-20"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("a20N"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("A20NN"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftTypeDesignator("A"));
    }

    @Test
    void testEmptyTypeDesignator() {
        // Empty type designator is allowed
        AircraftTypeDesignator atd = new AircraftTypeDesignator("");
        assertEquals("", atd.string());
    }
}