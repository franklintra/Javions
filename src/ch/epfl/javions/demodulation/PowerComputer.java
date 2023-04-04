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
    private final short[] last8Samples = new short[8]; // this is the buffer that will contain the last 8 samples used to calculate the power
    private final int batchSize;

    /**
     * Creates a new PowerComputer object that will read from the given input stream
     *
     * @param stream    the input stream to read from
     * @param batchSize the size of the batch of power samples to be calculated (must be a multiple of 8 and strictly greater than 0)
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % 8 == 0);
        this.batchSize = batchSize;
        this.decoder = new SamplesDecoder(stream, 2 * batchSize);
        this.sampleBuffer = new short[2 * batchSize];
    }

    /**
     * Reads a batch of samples from the input stream / necessary to calculate a batch of power samples
     *
     * @param batch the array of shorts that will contain the samples
     * @return number of samples read and written to the batch
     * @throws IOException              if input or output error occurs
     * @throws IllegalArgumentException if the size of the batch doesn't match the required size
     */
    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int read = decoder.readBatch(sampleBuffer);
        int last8Index = 0; // this is the index of the last sample that was added in the last8Samples buffer

        for (int i = 0; i < read / 2; i++) {
            // turnover latest data in last8Samples
            last8Samples[base8Mod(last8Index)] = sampleBuffer[2 * i];
            last8Samples[base8Mod(last8Index + 1)] = sampleBuffer[2 * i + 1];
            int evenIndexes = last8Samples[base8Mod(last8Index - 6)] - last8Samples[base8Mod(last8Index - 4)] + last8Samples[base8Mod(last8Index - 2)] - last8Samples[last8Index];
            int oddIndexes = last8Samples[base8Mod(last8Index - 5)] - last8Samples[base8Mod(last8Index - 3)] + last8Samples[base8Mod(last8Index - 1)] - last8Samples[last8Index + 1];
            batch[i] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;
            last8Index = (last8Index + 2) % 8;
        }
        return read / 2;
    }

    /**
     * This method is used to calculate the modulus of a number with 8
     * In our case, we need to calculate the modulus of a number with 8, but the % operator in java gives the remainder instead of the modulus
     * Hence for array indexes, we need to use this method instead of the % operator to not get negative indexes
     * For optimization purposes, we avoid Math.floorMod() as we only care about a few negative numbers cases
     * (The value passed to this method is always strictly between -7 and 8)
     * This method is much faster than Math.floorMod() as it only needs to check if the number is negative and add 8 to it (25% gain in performance on all Tests)
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base 8
     */
    private static int base8Mod(int index) {
        return index < 0 ? index + 8 : index;
    }
}