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
    private static final int batchSize = (int) Math.scalb(1, 16);
    private final PowerComputer computer;
    private final int windowSize;
    private final int[] evenWindow;
    private final int[] oddWindow;
    private final int[] window; //we will consider the window as a circular array
    private long windowOldestIndex = 0;
    private long position = 0;
    private int batchIndex = 0;
    private int samplesCalculated;
    /**
     * Constructs a new PowerWindow object with the given stream and window size.
     *
     * @param stream     the stream to read from
     * @param windowSize the size of the window
     * @throws IOException              if the stream cannot be read
     * @throws IllegalArgumentException if the window size is not in the range [1, 2^16]
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= batchSize);
        this.windowSize = windowSize;
        this.computer = new PowerComputer(stream, batchSize);
        evenWindow = new int[batchSize];
        oddWindow = new int[batchSize];
        window = new int[windowSize];
        readBatch();
        advanceBy(windowSize);
    }

    /**
     * This method is used to calculate the modulus of a number with the size of the window
     *
     * @param index the number to calculate the modulus of
     * @return the modulus of the number base windowSize
     */
    private int baseWindowMod(long index) {
        return Math.floorMod(index, windowSize);
    }

    private void readBatch() throws IOException {
        // odd when batchIndex is 0, even when batchIndex is 1 (because batchIndex is incremented at the end of the method)
        if (batchIndex % 2 == 0) {
            samplesCalculated = computer.readBatch(oddWindow);
        } else {
            samplesCalculated = computer.readBatch(evenWindow);
        }
        batchIndex = (batchIndex + 1) % 2;
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
        return position - windowSize;
    }


    /**
     * @return true if the window is full, false otherwise
     */
    public boolean isFull() {
        return position % batchSize <= samplesCalculated;
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
     * Advances the window by one sample by reading the next sample from the stream.
     *
     * @throws IOException if the stream cannot be read / if the window is full
     */
    public void advance() throws IOException {
        //samplesLeft--;
        if (position % batchSize == 0 && position != 0) {
            readBatch();
        }
        if (batchIndex % 2 == 0) {
            window[baseWindowMod(windowOldestIndex)] = evenWindow[(int) ((position) % (batchSize))];
        } else {
            window[baseWindowMod(windowOldestIndex)] = oddWindow[(int) ((position) % batchSize)];
        }
        windowOldestIndex++;
        position++;
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
     * This method is used to print beautifully the window as an array shape
     */
    @SuppressWarnings("unused")
    public void printArray() {
        System.out.print("[");
        for (int i = 0; i < windowSize; i++) {
            System.out.printf("%d, ", window[baseWindowMod(windowOldestIndex + i)]);
        }
        System.out.printf("]%n");
    }
}