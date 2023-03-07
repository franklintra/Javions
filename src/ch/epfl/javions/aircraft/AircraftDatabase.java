package ch.epfl.javions.aircraft;

//import jdk.internal.icu.impl.Punycode;

import java.io.*;
import java.net.URLDecoder;
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
        String zipPath = Objects.requireNonNull(getClass().getResource("/aircraft.zip")).getFile();
        zipPath = URLDecoder.decode(zipPath, UTF_8);

        try (ZipFile zipFile = new ZipFile(new File(zipPath))) {
            List<? extends ZipEntry> zipEntries = zipFile.stream().toList();

            for (ZipEntry z: zipEntries) {
                nextZip :
                try (
                        InputStream inputStream = zipFile.getInputStream(z);
                        Reader streamReader = new InputStreamReader(inputStream, UTF_8);
                        BufferedReader bufferedReader = new BufferedReader(streamReader)
                )
                {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if ((line.split(",", -1)[0]).compareTo(address.string()) > 0) {
                            break nextZip; //Interrupts the loop and go to the next zip file if the current address is greater than the address we're looking for (because the database is sorted)
                        }
                        if (line.startsWith(address.string())) {
                            System.out.println(z.getName());
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
                }
            }
        }
        catch (IOException e) {
            throw new IOException("Could not read database file: " + filename);
        }
        throw new IOException("No aircraft found for address: " + address.string());
    }
}
