package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDataTest {
    @Test
    void aircraftDataConstructorThrowsWithNullAttribute() {
        var registration = new AircraftRegistration("HB-JAV");
        var typeDesignator = new AircraftTypeDesignator("B738");
        var model = "Boeing 737-800";
        var description = new AircraftDescription("L2J");
        var wakeTurbulenceCategory = WakeTurbulenceCategory.LIGHT;
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(null, typeDesignator, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, null, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, null, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, null, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, description, null);
        });
    }
}