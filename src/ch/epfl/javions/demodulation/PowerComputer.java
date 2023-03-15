package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author @franklintra
 * @author @chukla
 * @project Javions
 */

public final class PowerComputer {

    private final SamplesDecoder decoder;
    private final short[] sampleBuffer; // this is the buffer that will contain the samples read from the input stream.
    private final int[] last8Samples = new int[8]; // this is the buffer that will contain the last 8 samples used to calculate the power
    private final int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % 8 == 0);
        this.batchSize = batchSize;
        this.decoder = new SamplesDecoder(stream, batchSize);
        this.sampleBuffer = new short[batchSize];
    }

    /**
     * This method is used to calculate the modulus of a number with 8
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base 8
     */
    private int base8Mod(int index) {
        return Math.floorMod(index, 8); // this is used so that it works with negative values (the % operator in java gives the remainder instead of the modulus)
    }

    /**
     * Reads a batch of samples from the input stream / necessary to calculate a batch of power samples
     * @param batch the array of shorts that will contain the samples
     * @return number of samples read and written to the batch
     * @throws IOException if input or output error occurs
     * @throws IllegalArgumentException if the size of the batch doesn't match the required size
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int bufferIndex = 2;
        int written = 0;
        int read = decoder.readBatch(sampleBuffer); //read first batch of samples
        //put latest data in last8Samples
        last8Samples[6] = sampleBuffer[0];
        last8Samples[7] = sampleBuffer[1];

        for (int i = 2; i < batchSize+batchSize+2; i+=2) {
            if (Math.floorMod(i, batchSize) == 0) {
                read += decoder.readBatch(sampleBuffer); //decode new data only if needed
                bufferIndex = 0;
            }
            int evenIndexes = last8Samples[base8Mod((bufferIndex - 2) - 6)] - last8Samples[base8Mod((bufferIndex - 2) - 4)] + last8Samples[base8Mod((bufferIndex - 2) - 2)] - last8Samples[base8Mod((bufferIndex - 2))];
            int oddIndexes = last8Samples[base8Mod((bufferIndex - 2) - 5)] - last8Samples[base8Mod((bufferIndex - 2) - 3)] + last8Samples[base8Mod((bufferIndex - 2) - 1)] - last8Samples[base8Mod((bufferIndex - 2) + 1)];
            batch[i / 2 - 1] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;

            // turnover latest data in last8Samples
            last8Samples[base8Mod(bufferIndex)] = sampleBuffer[bufferIndex];
            last8Samples[base8Mod((bufferIndex) + 1)] = sampleBuffer[bufferIndex + 1];
            bufferIndex = Math.floorMod(bufferIndex, sampleBuffer.length - 2);
            written++;
        }
        return written;
    }
}