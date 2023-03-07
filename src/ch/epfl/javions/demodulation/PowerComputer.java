package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @author @chukla
 * @project Javions
 */

public final class PowerComputer {

    private final SamplesDecoder decoder;
    private final short[] sampleBuffer; // this is the buffer that will contain the samples read from the input stream.
    private final int[] last8Samples = new int[8]; // this is the buffer that will contain the last 8 samples used to calculate the power
    private static int bufferIndex = 0; // this is the index of the oldest sample in the buffer / default is 0
    private final int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % 8 == 0);
        this.batchSize = batchSize;
        this.decoder = new SamplesDecoder(stream, batchSize);
        this.sampleBuffer = new short[batchSize];
    }

    /**
     * Reads a batch of samples from the input stream / necessary to calculate a batch of power samples
     * @param batch the array of shorts that will contain the samples
     * @return number of samples read and written to the batch
     * @throws IOException if input or output error occurs
     * @throws IllegalArgumentException if the size of the batch doesn't match the required size
     * fixme : this method is not working properly yet. Still need to fix it. (lookup instructions)
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int written = 0;
        decoder.readBatch(sampleBuffer);
        /*
        for (int i=0; i < batchSize; i+=1) {

            int left = sampleBuffer[bufferIndex - 6] - sampleBuffer[bufferIndex - 4] + sampleBuffer[bufferIndex - 2] - sampleBuffer[bufferIndex];
            int right = sampleBuffer[bufferIndex - 7] - sampleBuffer[bufferIndex - 5] + sampleBuffer[bufferIndex - 3] - sampleBuffer[bufferIndex - 1];
            batch[i/2] = left * left + right * right;
            written++;
            bufferIndex = (bufferIndex + 1) % 8;
        }*/

        //put data in last8Samples
        for (int i = 0; i < 8; i += 1) {
            last8Samples[i] = sampleBuffer[i];
        }

        for (int i = 0; i < batchSize; i += 2) {
            int left = last8Samples[bufferIndex - 6] - last8Samples[bufferIndex - 4] + last8Samples[bufferIndex - 2] - last8Samples[bufferIndex];
            int right = last8Samples[bufferIndex - 7] - last8Samples[bufferIndex - 5] + last8Samples[bufferIndex - 3] - last8Samples[bufferIndex - 1];
            batch[i / 2] = left * left + right * right;

            last8Samples[bufferIndex] = sampleBuffer[i];
            bufferIndex = (bufferIndex + 1) % 8;
        }

        return written;
    }
}
