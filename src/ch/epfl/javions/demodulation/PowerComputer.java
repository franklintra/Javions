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
    private int bufferIndex = 0; // this is the index of the oldest sample in the buffer / default is 0
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
     * This method is used to set the index of the oldest sample in the buffer
     * @param newIndex the new index of the oldest sample in the buffer
     * @return the value calculated by the method and set as to bufferIndex
     */
    private int setBufferIndex(int newIndex) {
        bufferIndex = base8Mod(newIndex);
        return bufferIndex;
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
        //put latest data in last8Samples
        for (int i = 0; i < 8; i += 1) {
            last8Samples[i] = sampleBuffer[i];
        }

        for (int i = 0; i < batchSize; i++) {
            if (base8Mod(i) == 0) {
                decoder.readBatch(sampleBuffer); //decode new data only if needed
            }
            int evenIndexes = last8Samples[base8Mod(bufferIndex-6)] - last8Samples[base8Mod(bufferIndex-4)] + last8Samples[base8Mod(bufferIndex-2)] - last8Samples[base8Mod(bufferIndex)];
            int oddIndexes = last8Samples[base8Mod(bufferIndex-7)] - last8Samples[base8Mod(bufferIndex-5)] + last8Samples[base8Mod(bufferIndex-3)] - last8Samples[base8Mod(bufferIndex-1)];
            System.out.println(Arrays.toString(last8Samples));
            System.out.println(evenIndexes + " " + oddIndexes);
            batch[i] = evenIndexes * evenIndexes + oddIndexes * oddIndexes;

            // turnover latest data in last8Samples
            last8Samples[bufferIndex] = sampleBuffer[bufferIndex];
            setBufferIndex(bufferIndex+1);
        }
        return written;
    }
}