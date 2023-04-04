package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @author @chukla
 * @project Javions
 */
public final class PowerWindow {
    private static final int BATCH_SIZE = (int) Math.scalb(1, 16);
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
        if ((position < 0) || (position >= windowSize)) {
            throw new IndexOutOfBoundsException("Position " + position + " is out of bounds.");
        }
        return window[baseWindowMod(windowOldestIndex + position)];
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
            window[baseWindowMod(windowOldestIndex)] = evenWindow[(int) (windowOldestIndex % BATCH_SIZE)];
        } else {
            window[baseWindowMod(windowOldestIndex)] = oddWindow[(int) (windowOldestIndex % BATCH_SIZE)];
        }
        windowOldestIndex++;
    }

    /**
     * Advances the window by the given number of samples by reading the next samples from the stream.
     *
     * @param n the number of samples to advance by
     * @throws IOException              if the stream cannot be read / if the window is full
     * @throws IllegalArgumentException if the number of samples to advance by is negative
     */
    public void advanceBy(int n) throws IOException {
        Preconditions.checkArgument(n >= 0);
        for (int i = 0; i < n; i++) {
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
     * It casts the number to an int to use it directly as an index in the window array.
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base windowSize
     */
    private int baseWindowMod(long index) {
        return (int) (index % windowSize);
    }
}