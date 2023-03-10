package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
@SuppressWarnings("unused")
class PowerComputerTest {
    /**
     * Tests that the first 10 values obtained from PowerComputer are [73 292 65 745 98 4226 12244 25722 36818 23825] for the samples.bin file.
     *
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the path is null (file not found)
     */
    @Test
    void testFirst10ValuesReadBatch() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/samples.bin");
        int batchSize = (int) Math.scalb(1, 3);
        PowerComputer decoder = new PowerComputer(stream, batchSize);
        int[] batch = new int[batchSize];
        int numberOfSamples = decoder.readBatch(batch);
        /*for (int i = 0; i < 1000; i++) {
            System.out.println(i + " " + batch[i]);
        }*/
        assertArrayEquals(new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722}, batch);
        assertEquals(8, numberOfSamples);
    }
}