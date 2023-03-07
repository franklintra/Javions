package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @chukla
 * @project Javions
 */

public final class PowerComputer {

    private final SamplesDecoder decoder;
    private short[] sampleBuffer;
    private final int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {
        this.batchSize = batchSize;
        Preconditions.checkArgument(batchSize % 8 == 0 && batchSize > 0, "Batch size must be a multiple of 8 and strictly positive");

        decoder = new SamplesDecoder(stream, batchSize);
        sampleBuffer = new short[batchSize];
    }

    public int readBatch(int[] batch) throws IOException {
        int samplesRead = decoder.readBatch(sampleBuffer);

        Preconditions.checkArgument(samplesRead == sampleBuffer.length, "Batch size must be equal to the size of a lot");


        return samplesRead;
    }
}
