package ch.epfl.javions.aircraft;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public final class AircraftDatabase {
    private final String filename;
    /**
     * The constructor of the AircraftDatabase class
     * @param filename the name of the database file
     * @throws IOException if the database file could not be read
     */
    public AircraftDatabase(String filename) throws IOException{
        Objects.requireNonNull(filename);
        this.filename = filename;
        if(getClass().getResource(filename) == null) {
            throw new IOException("Could not read database file: " + filename);
        }
    }

    /**
     * Returns the aircraft data for the given ICAO address.
     * @param address the ICAO address of the aircraft
     * @return the aircraft data
     * @throws IOException if the database file could not be read
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        Objects.requireNonNull(address);
        URI d;
        try {
            d = Objects.requireNonNull(getClass().getResource(filename)).toURI();
        } catch (URISyntaxException ignored) {
            return null;
        }

        try (ZipFile zipFile = new ZipFile(new File(d))) {
            List<? extends ZipEntry> zipEntries = zipFile.stream().toList();

            for (ZipEntry z: zipEntries) {
                try (
                        InputStream inputStream = zipFile.getInputStream(z);
                        Reader streamReader = new InputStreamReader(inputStream, UTF_8);
                        BufferedReader bufferedReader = new BufferedReader(streamReader)
                )
                {
                    String line = bufferedReader.readLine();
                    while (line != null) {
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
                        line = bufferedReader.readLine();
                    }
                }
            }
        }
        catch (IOException e) {
            throw new IOException("Could not read database file: " + filename);
        }
        throw new IOException("No aircraft found for address: " + address.string());
    }
}
