package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDescriptionTest {
    @Test
    void testValidDescription() {
        AircraftDescription aD1 = new AircraftDescription("A0E");
        assertEquals("A0E", aD1.string());
        AircraftDescription aD2 = new AircraftDescription("-8-");
        assertEquals("-8-", aD2.string());
    }

    @Test
    void testInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("A0"));
        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("a0E"));
    }
}