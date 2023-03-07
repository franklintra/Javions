package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unused")
class AircraftRegistrationTest {

    @Test
    void testValidRegistration() {
        AircraftRegistration reg = new AircraftRegistration("HB-JDC");
        assertEquals("HB-JDC", reg.string());
    }

    @Test
    void testInvalidRegistration() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftRegistration("$%&^*"));
    }
}