package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author @franklintra
 * @project Javions
 */
public final class AircraftDatabase {
    private final String filename;
    /**
     * Creates a new AircraftDatabase object.
     * @param filename the path to the database file
     */
    public AircraftDatabase(String filename) {
        Objects.requireNonNull(filename);
        this.filename = filename;
    }

    /**
     * Returns the aircraft data for the given ICAO address if it exists in the database, null otherwise.
     * @param address ICAO address
     * @return aircraft data
     * @throws IOException if the database cannot be read
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        String d = Objects.requireNonNull(getClass().getResource("/aircraft.zip")).getFile();
        try (ZipFile zipFile = new ZipFile(d);
             InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(filename));
             Reader streamReader = new InputStreamReader(inputStream, UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader))
        {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.split(",")[0].compareTo(address.toString()) > 0) {
                    break; //Interrupts the loop if the current address is greater than the address we're looking for (because the database is sorted)
                }
                if (line.startsWith(address.string())) {
                    String[] data = line.split(",");
                    return new AircraftData(
                            new AircraftRegistration(data[1]),
                            new AircraftTypeDesignator(data[2]),
                            data[3],
                            new AircraftDescription(data[4]),
                            WakeTurbulenceCategory.of(data[5])
                    );
                }
            }
        }
        catch (IOException e) {
            throw new IOException("Could not read database file: " + filename);
        }
        return null; // if the address is not found in the database
    }
}
