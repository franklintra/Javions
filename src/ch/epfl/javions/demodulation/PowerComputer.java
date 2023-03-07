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
        Preconditions.checkArgument(batchSize % 8 == 0 && batchSize > 0);

        decoder = new SamplesDecoder(stream, batchSize);
        sampleBuffer = new short[batchSize];
    }

    public int readBatch(int[] batch) throws IOException {
        int samplesRead = decoder.readBatch(sampleBuffer);

        Preconditions.checkArgument(samplesRead == sampleBuffer.length);


        return samplesRead;
    }
}
