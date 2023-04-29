package ch.epfl.javions.aircraft;

//import jdk.internal.icu.impl.Punycode;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */
public final class AircraftDatabase {
    private final String filename;

    /**
     * The constructor of the AircraftDatabase class
     *
     * @param filename the name of the database file
     * @throws NullPointerException if the database file could not be read
     */
    public AircraftDatabase(String filename) {
        Objects.requireNonNull(filename);
        this.filename = filename;
    }

    /**
     * Returns the aircraft data for the given ICAO description.
     *
     * @param address the ICAO description of the aircraft
     * @return the aircraft data
     * @throws IOException if the database file could not be read
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        Objects.requireNonNull(address);
        try (ZipFile zipFile = new ZipFile(new File(filename))) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                nextZip:
                try (
                        BufferedReader file = new BufferedReader(new InputStreamReader(
                                zipFile.getInputStream(
                                        zipEntries.nextElement()
                                )
                        ))
                ) {
                    String line;
                    while ((line = file.readLine()) != null) {
                        int comparison = line.substring(0, IcaoAddress.LENGTH).compareTo(address.string());
                        if (comparison > 0) {
                            break nextZip; //Interrupts the loop and go to the next zip file if the current description is greater than the description we're looking for (because the database is sorted)
                        }
                        if (comparison == 0) {
                            return parseAircraftData(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not read database file: " + filename + " (" + e.getMessage() + ")", e);
        }
        return null;
    }

    /**
     * Parses a line of the database file and returns the corresponding AircraftData object.
     *
     * @param line the line to parse
     * @return the AircraftData object corresponding to the line
     */
    private AircraftData parseAircraftData(String line) {
        String[] data = line.split(",", -1);
        return new AircraftData(
                new AircraftRegistration(data[1]),
                new AircraftTypeDesignator(data[2]),
                data[3],
                new AircraftDescription(data[4]),
                WakeTurbulenceCategory.of(data[5])
        );
    }
}
