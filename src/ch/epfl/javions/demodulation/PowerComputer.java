package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra (362694)
 * @project Javions
 * This class is used to calculate the power of a batch of samples
 * It is used by the PowerWindow class to calculate the power of the samples in the window
 * It allows to demodulate messages from the samples in combination with the PowerWindow and SamplesDecoder classes.
 */
public final class PowerComputer {
    private final SamplesDecoder decoder;
    private final short[] sampleBuffer; // this is the buffer that will contain the samples read from the input stream.
    private static final int N = 8; // the number of samples used to calculate the power

    private final short[] lastNSamples = new short[N]; // this is the buffer that will contain the last 8 samples used to calculate the power
    private final int batchSize;

    /**
     * Creates a new PowerComputer object that will read from the given input stream
     *
     * @param stream    the input stream to read from
     * @param batchSize the size of the batch of power samples to be calculated (must be a multiple of 8 and strictly greater than 0)
     * @throws IllegalArgumentException if the batch size is not a multiple of 8 or is negative
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % N == 0);
        this.batchSize = batchSize;
        this.decoder = new SamplesDecoder(stream, 2 * batchSize);
        this.sampleBuffer = new short[Short.BYTES * batchSize];
    }

    /**
     * Reads a batch of samples from the input stream / necessary to calculate a batch of power samples
     *
     * @param batch the array of shorts that will contain the samples
     * @return number of samples read and written to the batch
     * @throws IOException              if input or output error occurs
     * @throws IllegalArgumentException if the size of the batch doesn't match the required size
     */
    public int readBatch(int... batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int read = decoder.readBatch(sampleBuffer);
        int lastNIndex = 0; // this is the index of the last sample that was added in the last8Samples buffer

        for (int i = 0; i < read / 2; i++) {
            // turnover latest data in last8Samples
            lastNSamples[baseNMod(lastNIndex)] = sampleBuffer[2 * i];
            lastNSamples[baseNMod(lastNIndex + 1)] = sampleBuffer[2 * i + 1];
            int evenIndexes = lastNSamples[baseNMod(lastNIndex - 6)] - lastNSamples[baseNMod(lastNIndex - 4)] + lastNSamples[baseNMod(lastNIndex - 2)] - lastNSamples[lastNIndex];
            int oddIndexes = lastNSamples[baseNMod(lastNIndex - 5)] - lastNSamples[baseNMod(lastNIndex - 3)] + lastNSamples[baseNMod(lastNIndex - 1)] - lastNSamples[lastNIndex + 1];
            batch[i] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;
            lastNIndex = (lastNIndex + 2) % N;
        }
        return read / 2;
    }

    /**
     * This method is used to calculate the modulus of a number with N
     * In our case, we need to calculate the modulus of a number with N, but the % operator in java gives the remainder instead of the modulus
     * Hence for array indexes, we need to use this method instead of the % operator to not get negative indexes
     * For optimization purposes, we avoid Math.floorMod() as we only care about a few negative numbers cases
     * (The value passed to this method is always strictly between -N+1 and N)
     * This method is much faster than Math.floorMod() as it only needs to check if the number is negative and add N to it (25% gain in performance on all Tests)
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base 8
     */
    private static int baseNMod(int index) {
        return index < 0 ? index + N : index;
    }
}