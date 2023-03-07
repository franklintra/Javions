package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class AircraftDataTest {

    /**
     * Checks that AircraftData.equals() returns true when two AircraftData objects are equal
     */
    @Test
    void testAircraftDataEquals() {
        AircraftData data1 = new AircraftData(
                new AircraftRegistration("ABC123"),
                new AircraftTypeDesignator("A320"),
                "Airbus A320",
                new AircraftDescription("L2J"),
                WakeTurbulenceCategory.MEDIUM
        );

        AircraftData data2 = new AircraftData(
                new AircraftRegistration("ABC123"),
                new AircraftTypeDesignator("A320"),
                "Airbus A320",
                new AircraftDescription("L2J"),
                WakeTurbulenceCategory.MEDIUM
        );


        // Test equality of data1 and data2
        Assertions.assertEquals(data1, data2);

    }

}