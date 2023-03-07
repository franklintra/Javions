package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDescriptionTest {
    @Test
    void aircraftDescriptionConstructorThrowsWithInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftDescription("abc");
        });
    }

    @Test
    void aircraftDescriptionConstructorAcceptsEmptyDescription() {
        assertDoesNotThrow(() -> {
            new AircraftDescription("");
        });
    }

    @Test
    void aircraftDescriptionConstructorAcceptsValidDescription() {
        assertDoesNotThrow(() -> {
            new AircraftDescription("A0E");
        });
    }
}