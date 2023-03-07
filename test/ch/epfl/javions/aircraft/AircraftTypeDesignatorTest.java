package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AircraftTypeDesignatorTest {
    @Test
    void aircraftTypeDesignatorConstructorThrowsWithInvalidTypeDesignator() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftTypeDesignator("ABCDE");
        });
    }

    @Test
    void aircraftTypeDesignatorConstructorAcceptsEmptyTypeDesignator() {
        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("");
        });
    }

    @Test
    void aircraftTypeDesignatorConstructorAcceptsValidTypeDesignator() {
        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("BCS3");
        });
    }
}