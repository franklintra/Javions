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
     * Constructs a new PowerComputer object with the given stream and batch size.
     *
     * @param stream    the stream to read from
     * @param batchSize the size of the batch
     * @throws IOException              if the stream cannot be read
     * @throws IllegalArgumentException if the batch size is not a multiple of 8
     */
    public PowerComputer(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0 && batchSize % 8 == 0);
        this.batchSize = batchSize;
        this.decoder = new SamplesDecoder(stream, batchSize);
        this.sampleBuffer = new short[batchSize];
    }

    /**
     * This method is used to calculate the modulus of a number with 8
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base 8
     */
    private int base8Mod(int index) {
        return Math.floorMod(index, 8); // this is used so that it works with negative values (the % operator in java gives the remainder instead of the modulus)
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
        int lastCounter;
        Preconditions.checkArgument(batch.length == batchSize);
        int bufferIndex = 2;
        int written = 0;
        lastCounter = decoder.readBatch(sampleBuffer);
        //put latest data in last8Samples
        last8Samples[6] = sampleBuffer[0];
        last8Samples[7] = sampleBuffer[1];

        for (int i = 2; i < batchSize + batchSize + 2; i += 2) {
            if (Math.floorMod(i, batchSize) == 0 && i != 0) {
                lastCounter += decoder.readBatch(sampleBuffer); //decode new data only if needed
                bufferIndex = 0;
            }
            if (lastCounter == 0) {
                return written;
            }
            int evenIndexes = last8Samples[base8Mod((bufferIndex - 2) - 6)] - last8Samples[base8Mod((bufferIndex - 2) - 4)] + last8Samples[base8Mod((bufferIndex - 2) - 2)] - last8Samples[base8Mod((bufferIndex - 2))];
            int oddIndexes = last8Samples[base8Mod((bufferIndex - 2) - 5)] - last8Samples[base8Mod((bufferIndex - 2) - 3)] + last8Samples[base8Mod((bufferIndex - 2) - 1)] - last8Samples[base8Mod((bufferIndex - 2) + 1)];
            batch[i / 2 - 1] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;

            // turnover latest data in last8Samples
            last8Samples[base8Mod(bufferIndex - 2)] = sampleBuffer[Math.floorMod(bufferIndex, sampleBuffer.length)];
            last8Samples[base8Mod((bufferIndex - 2) + 1)] = sampleBuffer[Math.floorMod(bufferIndex + 1, sampleBuffer.length)];
            bufferIndex = Math.floorMod(bufferIndex + 2, sampleBuffer.length);
            written++;
            lastCounter -= 2;
        }
        return lastCounter * batchSize * 2 + written * 4;
    }
}