package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftRegistrationTest {

    @Test
    void testValidRegistration() {
        AircraftRegistration reg = new AircraftRegistration("HB-JDC");
        assertEquals("N12345", reg.string());
    }

    @Test
    void testInvalidRegistration() {
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration reg = new AircraftRegistration("$%&^*");
        });
    }
}