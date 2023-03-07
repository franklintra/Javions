package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author @franklintra
 * @project Javions
 */
class SamplesDecoderTest {
    @Test
    void testSampleIsNotNull() {
        assertThrows(NullPointerException.class, () -> new SamplesDecoder(null, 8));
    }

    @Test
    void testBatchSizePositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SamplesDecoder(InputStream.nullInputStream(), -1);
        });
    }

    @Test
    void testBatchSizeEqualToArrayLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SamplesDecoder(InputStream.nullInputStream(), 8).readBatch(new short[9]);
        });
    }

    @Test
    void testReadBatchIOException() {
        InputStream stream = new BufferedInputStream(getClass().getResourceAsStream("/test.bin"));

        short[] buffer = new short[8];
        SamplesDecoder decoder = new SamplesDecoder(stream, 8);

        assertThrows(IOException.class, () -> decoder.readBatch(buffer));

    }
}
