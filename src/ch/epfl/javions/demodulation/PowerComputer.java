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
    private final int batchSize;
    private boolean isWriting = false;

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
        return Math.floorMod(index, 8);// this is used so that it works with negative values (the % operator in java gives the remainder instead of the modulus)
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
        if (!isWriting) {
            decoder.readBatch(sampleBuffer); //read first batch of samples
            last8Samples[0] = sampleBuffer[0];
            last8Samples[1] = sampleBuffer[1];
        }
        int written = 0;
        int bufferIndex = 2;

        for (int i = 0; i < batchSize ; i += 1) {
            if (Math.floorMod(bufferIndex, batchSize) == 0) {
                decoder.readBatch(sampleBuffer); //decode new data only if needed
            }
            int evenIndexes = last8Samples[base8Mod(bufferIndex - 6)] - last8Samples[base8Mod(bufferIndex - 4)] + last8Samples[base8Mod(bufferIndex - 2)] - last8Samples[base8Mod((bufferIndex))];
            int oddIndexes = last8Samples[base8Mod(bufferIndex - 5)] - last8Samples[base8Mod(bufferIndex - 3)] + last8Samples[base8Mod(bufferIndex - 1)] - last8Samples[base8Mod((bufferIndex + 1))];
            batch[i] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;
            // turnover latest data in last8Samples
            last8Samples[base8Mod(bufferIndex)] = sampleBuffer[bufferIndex];
            last8Samples[base8Mod(bufferIndex + 1)] = sampleBuffer[bufferIndex + 1];
            bufferIndex = Math.floorMod(bufferIndex + 2, batchSize);
            written++;
        }
        isWriting = true;
        return written;
    }
}