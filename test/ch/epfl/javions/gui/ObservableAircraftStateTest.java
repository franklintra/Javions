package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.*;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class ObservableAircraftStateTest {

    @Test
    void test() {
            try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))){
                int counter = 0;
                byte[] bytes = new byte[RawMessage.LENGTH];
                while (s.available() >= bytes.length) {
                    long timeStampNs = s.readLong();
                    int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                    assertEquals(RawMessage.LENGTH, bytesRead);
                    ByteString message = new ByteString(bytes);
                    if (counter++ < 3) System.out.printf("%13d: %s\n", timeStampNs, message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    @Test
    void updateTable() {

        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))){
            AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(s.toString())); // FIXME: 4/11/2023 this is incorrect, unsure
            int counter = 0;
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() >= bytes.length) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                RawMessage rawMessage = RawMessage.of(timeStampNs, bytes);
                aircraftStateManager.updateWithMessage(rawMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
