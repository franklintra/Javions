package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
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
    void specialCaseStreamEndReached() {
        //todo: @Chukla write this testcase
        /*
        puis convertit comme décrit à la 2.4.1 ces octets en échantillons signés, qui sont placés dans le tableau
        passé en argument ; le nombre d'échantillons effectivement converti est retourné, et il est toujours
        égal à la taille du lot sauf lorsque la fin du flot a été atteinte avant d'avoir pu lire assez d'octets,
        auquel cas il est égal au nombre d'octets lus divisé par deux, arrondi vers le bas
         */
    }

    @Test
    void assertThrowsExceptionConstructor() {
        assertThrows(NullPointerException.class, () -> new SamplesDecoder(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new SamplesDecoder(new FileInputStream(Objects.requireNonNull(getClass().getResource("/samples.bin")).getFile()), -1));
    }
}