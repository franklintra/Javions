package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

/**
 * This class is used to compute the power of a stream of samples.
 * The power is computed using a sliding window of a given size.
 * It allows to demodulate messages from the samples in combination with the PowerComputer and SamplesDecoder classes.
 */
public final class PowerWindow {
    private static final int BATCH_SIZE = 1 << 16; // 2^16
    private final PowerComputer computer;
    private final int[] evenWindow;
    private final int[] oddWindow;
    private final int windowSize;
    private final int[] window; //we will consider the window as a circular array
    private long windowOldestIndex; // this is the index of the oldest sample in the window
    private byte batchIndex; // this is used to alternate between the even and odd batch (alternates between 0 and 1 hence the byte type)
    private int samplesCalculated; // this is used to determine whether the window is full or not (if we have reached the end of the stream)

    /**
     * Constructs a new PowerWindow object with the given stream and window size.
     *
     * @param stream     the stream to read from
     * @param windowSize the size of the window
     * @throws IOException              if the stream cannot be read
     * @throws IllegalArgumentException if the window size is not in the range [1, 2^16]
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= BATCH_SIZE);
        this.windowSize = windowSize;
        this.computer = new PowerComputer(stream, BATCH_SIZE);
        evenWindow = new int[BATCH_SIZE];
        oddWindow = new int[BATCH_SIZE];
        window = new int[windowSize];
        readBatch(); // we read the first batch to fill the even window
        advanceBy(windowSize); // we advance the window by the window size to fill it with the first samples
    }

    /**
     * @return the size of the window
     */
    public int size() {
        return windowSize;
    }

    /**
     * @return the current position of the window
     */
    public long position() {
        return windowOldestIndex - windowSize;
    }

    /**
     * @return true if the window is full, false otherwise
     */
    public boolean isFull() {
        return windowOldestIndex % BATCH_SIZE <= samplesCalculated;
    }

    /**
     * @param position the position of the sample to return
     * @return the sample at the given position
     * @throws IndexOutOfBoundsException if the position is out of bounds
     */
    public int get(int position) {
        Objects.checkIndex(position, windowSize);
        return window[baseWindowMod((int) (windowOldestIndex + position))];
    }

    /**
     * Advances the window by one sample by reading it from the buffer (either the even or odd window)
     * and reading it from the stream if we reached the end of the current buffer
     *
     * @throws IOException if the stream cannot be read / if the window is full
     */
    public void advance() throws IOException {
        if (windowOldestIndex % BATCH_SIZE == 0 && windowOldestIndex != 0) {
            readBatch();
        }
        if (batchIndex % 2 == 0) {
            window[baseWindowMod(windowOldestIndex)] = evenWindow[baseBatchMod(windowOldestIndex)];
        } else {
            window[baseWindowMod(windowOldestIndex)] = oddWindow[baseBatchMod(windowOldestIndex)];
        }
        windowOldestIndex++;
    }

    /**
     * Advances the window by the given number of samples by reading the next samples from the stream.
     *
     * @param offset the number of samples to advance by
     * @throws IOException              if the stream cannot be read / if the window is full
     * @throws IllegalArgumentException if the number of samples to advance by is negative
     */
    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }

    /**
     * This method is used to read a batch of samples from the stream. It is used to fill the even and odd window according to which batch was read last.
     * It is also used to alternate between the even and odd window by updating the batchIndex.
     *
     * @throws IOException if the stream cannot be read
     */
    private void readBatch() throws IOException {
        // odd when batchIndex is 0, even when batchIndex is 1 (because batchIndex is incremented at the end of the method)
        if (batchIndex % 2 == 0) {
            samplesCalculated = computer.readBatch(oddWindow);
        } else {
            samplesCalculated = computer.readBatch(evenWindow);
        }
        batchIndex = (byte) ((batchIndex + 1) % 2);
    }

    /**
     * This method is used to calculate the modulus of a number with the size of the window.
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base windowSize
     */
    private int baseWindowMod(long index) {
        return (int) index % windowSize;
    }

    private int baseBatchMod(long index) {
        return (int) index % BATCH_SIZE;
    }
}