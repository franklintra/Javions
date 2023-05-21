package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * @author @franklintra (362694)
 * @project Javions
 *
 * The SamplesDecoder class that decodes a batch of samples from an input stream. The samples are stored in an array of shorts.
 * This class is used by PowerComputer to decode the samples from the input stream.
 * In conjunction with the PowerWindow class, it is used to decode messages from the input stream.
 */
public final class SamplesDecoder {
    private static final int BIAS = 2048;
    private static final int BYTE_MASK = 0xFF;
    private final int batchSize; // the size of the batch of samples to read
    private final InputStream stream;
    private final byte[] buffer;

    /**
     * The constructor of the SamplesDecoder class that takes an input stream and a batch size
     *
     * @param stream    the input stream that contains the samples to decode / cannot be null
     * @param batchSize the size of the batch / cannot be negative
     * @throws NullPointerException     if the stream is null
     * @throws IllegalArgumentException if the batch size is negative
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        buffer = new byte[2 * batchSize];
    }

    /**
     * Reads a batch of samples from the input stream
     *
     * @param batch the array of shorts that will contain the samples
     * @return the number of samples read and actually stored in the batch (if the end of the stream is reached, it will be less than the batch size)
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the size of the batch doesn't match the required size
     */
    public int readBatch(short... batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize); //throws IllegalArgumentException if the size of the batch doesn't match the required size
        int data = stream.readNBytes(buffer, 0, Short.BYTES * batchSize);
        //if the number of bytes data is not equal to 2*batchSize, then we have reached the end of the stream

        for (int i = 0; i < data; i += 2) {
            //buffer[i] is a two bytes number that represents a sample ([0] and [1])
            //we need to apply the transformation described in the project statement to get the sample
            int sample = (((buffer[i + 1] & BYTE_MASK) << Byte.SIZE) | (buffer[i] & BYTE_MASK)) - BIAS;
            batch[i / 2] = (short) sample;
        }
        return data / Short.BYTES;
    }
}
