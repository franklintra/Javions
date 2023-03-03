package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AircraftDatabaseTest {

    /**
     * The database used for the tests
     */
    private static final AircraftDatabase db;

    static {
        try {
            db = new AircraftDatabase("/aircraft.zip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
     * Checks that AircraftDatabase.get() throws an IOException when the IcaoAddress is not in the database
     */
    @Test
    void testGetNonExistingAddress() {
        Assertions.assertThrows(IOException.class, () -> db.get(new IcaoAddress("123456")));
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
     * Checks that AircraftDatabase.get() throws an IOException when the file is not found
     */
    @Test
    void testGetNonExistingFile() {
        Assertions.assertThrows(IOException.class, () -> new AircraftDatabase("non-existing.txt"));
    }

    /**
     * Checks that AircraftDatabase.get() throws a NullPointerException when the file is null
     */
    @Test
    void testNullFile() {
        Assertions.assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }
}