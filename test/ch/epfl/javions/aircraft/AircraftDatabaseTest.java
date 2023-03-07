package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class AircraftDatabaseTest {

    /**
     * The database used for the tests
     */
    private static final AircraftDatabase db = new AircraftDatabase("/aircraft.zip");

    /**
     * Checks that AircraftDatabase.get() returns the correct AircraftData for a given IcaoAddress
     */

    @Test
    void testGetExistingAddress() throws IOException {
        IcaoAddress address = new IcaoAddress("4B1814");

        AircraftData actualData = db.get(address);

        AircraftData expectedData = new AircraftData(
                new AircraftRegistration("HB-JDC"),
                new AircraftTypeDesignator("A20N"),
                "AIRBUS A-320neo",
                new AircraftDescription("L2J"),
                WakeTurbulenceCategory.MEDIUM
        );
        Assertions.assertEquals(expectedData, actualData);
    }

    /**
     * Checks that AircraftDatabase.get() returns null when the IcaoAddress does not exist
     */
    @Test
    void testGetNonExistingAddress() throws IOException {
        assertNull(db.get(new IcaoAddress("123456")));
    }


    /**
     * Checks that AircraftDatabase.get() throws a NullPointerException when the IcaoAddress is null
     */
    @Test
    void testGetNullAddress() {
        IcaoAddress address = null;
        Assertions.assertThrows(NullPointerException.class, () -> db.get(address));
    }

    /**
     * Checks that AircraftDatabase.get() throws a NullPointerException when the file is null
     */
    @Test
    void testNullFile() {
        Assertions.assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }

}