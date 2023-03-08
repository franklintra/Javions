package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
@SuppressWarnings("unused")
class PowerComputerTest {
    /**
     * Tests that the first 10 values obtained from PowerComputer are [73 292 65 745 98 4226 12244 25722 36818 23825] for the samples.bin file.
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the path is null (file not found)
     */
    @Test
    void testFirst10ValuesReadBatch() throws IOException {
        FileInputStream stream = new FileInputStream(Objects.requireNonNull(getClass().getResource("/samples.bin")).getFile());
        PowerComputer decoder = new PowerComputer(stream, 8);
        int[] batch = new int[8];
        decoder.readBatch(batch);
        System.out.println(Arrays.toString(batch));
        System.out.println(Arrays.toString(new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722}));
        assertArrayEquals(new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722}, batch);
    }

    //todo: do the other tests for this class : not necessary as long as the first one doesn't pass
}