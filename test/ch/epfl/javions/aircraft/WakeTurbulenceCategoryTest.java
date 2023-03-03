package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class WakeTurbulenceCategoryTest {

    /**
     * Checks that WakeTurbulenceCategory.of() returns the correct value for each string
     */

    @Test
    void of() {
        assertEquals(WakeTurbulenceCategory.LIGHT, WakeTurbulenceCategory.of("L"));
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.of("M"));
        assertEquals(WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.of("H"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("X"));
    }
}

