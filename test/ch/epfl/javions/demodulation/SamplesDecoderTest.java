package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
@SuppressWarnings("unused")
class SamplesDecoderTest {
    /**
     * Tests that the first 10 values obtained from SamplesDecoder are [-3 8 -9 -8 -5 -8 -12 -16 -23 -9] for the samples.bin file.
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the path is null (file not found)
     */
    @Test
    void testFirst10ValuesReadBatch() throws IOException {
        FileInputStream stream = new FileInputStream(Objects.requireNonNull(getClass().getResource("/samples.bin")).getFile());
        SamplesDecoder decoder = new SamplesDecoder(stream, 10);
        short[] batch = new short[10];
        decoder.readBatch(batch);
        assertArrayEquals(batch, new short[]{-3, 8, -9, -8, -5, -8, -12, -16, -23, -9});
    }

    @Test
    void assertThrowsReadBatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            FileInputStream stream = new FileInputStream(Objects.requireNonNull(getClass().getResource("/samples.bin")).getFile());
            SamplesDecoder decoder = new SamplesDecoder(stream, 10);
            short[] batch = new short[11];
            decoder.readBatch(batch);
        });
        assertThrows(IOException.class, () -> {
            FileInputStream stream = new FileInputStream(String.valueOf(getClass().getResource("/doesntexist.bin")));
            SamplesDecoder decoder = new SamplesDecoder(stream, 10);
            short[] batch = new short[10];
            decoder.readBatch(batch);
        });
    }

    @Test
    void specialCaseStreamEndReached() throws IOException { //fixme this test is not working (it doesn't test with right value bcs a program not implementing the special case would pass)
        InputStream fixedSizeInputStream = new ByteArrayInputStream(new byte[]{0, 1, 0, 1, 0, 1, 0, 1, 0});
        short[] buffer = new short[10];
        SamplesDecoder decoder = new SamplesDecoder(fixedSizeInputStream, 10);
        int data = decoder.readBatch(buffer);
        assertEquals(4, data);
    }

    @Test
    void assertThrowsExceptionConstructor() {
        assertThrows(NullPointerException.class, () -> new SamplesDecoder(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new SamplesDecoder(new FileInputStream(Objects.requireNonNull(getClass().getResource("/samples.bin")).getFile()), -1));
    }
}