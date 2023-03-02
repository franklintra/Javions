package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @project Javions
 */
public final class SamplesDecoder {
    private final int batchSize;
    private final InputStream stream;

    /**
     * The constructor of the SamplesDecoder class that takes an input stream and a batch size
     * @param stream the input stream that contains the samples to decode / cannot be null
     * @param batchSize the size of the batch / cannot be negative
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        if (stream == null) {
            throw new NullPointerException("The stream is null");
        }
        this.stream = stream;
        this.batchSize = batchSize;
    }

    /**
     * Reads a batch of samples from the input stream
     * @param batch the array of shorts that will contain the samples / cannot be null
     * @return the number of samples read //fixme not sure about this
     * @throws IOException if an I/O error occurs
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        //The first 4 shorts value are the lightweight bytes

        return 0;
    }
}
