package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WakeTurbulenceCategoryTest {
    @Test
    void wakeTurbulenceCategoryOfWorks() {
        assertEquals(WakeTurbulenceCategory.LIGHT, WakeTurbulenceCategory.of("L"));
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.of("M"));
        assertEquals(WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.of("H"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("X"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("l"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("m"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("h"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of(""));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("LIGHT"));
    }
}