package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
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
        int batchSize = 8;
        PowerComputer decoder = new PowerComputer(stream, batchSize);
        int[] batch = new int[batchSize];
        decoder.readBatch(batch);
        assertArrayEquals(new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722}, batch);
    }

    @Test
    void testReadBatch() throws IOException {
        // Create an input stream with sample data
        byte[] sampleData = new byte[16];
        for (int i = 0; i < 16; i += 2) {
            short sample = (short) (i * 100);
            sampleData[i] = (byte) (sample & 0xff);
            sampleData[i + 1] = (byte) ((sample >> 8) & 0xff);
        }
        InputStream inputStream = new ByteArrayInputStream(sampleData);

        // Create a PowerComputer with a batch size of 8
        PowerComputer powerComputer = new PowerComputer(inputStream, 8);

        // Read a batch of samples and calculate power
        int[] powerBatch = new int[8];
        int numSamples = powerComputer.readBatch(powerBatch);

        // Verify that the correct number of samples was read
        assertEquals(8, numSamples);

        // Verify that the power calculation is correct
        assertArrayEquals(new int[]{}, powerBatch); //// TODO: 3/9/2023  need to figure out how to get expected values

    }
}