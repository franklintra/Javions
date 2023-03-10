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
    //private static final int batchSize = 800;
    private final PowerComputer computer;
    private final int windowSize;
    private final int[] evenWindow;
    private final int[] oddWindow;
    private final int[] window; //we will consider the window as a circular array
    private int windowOldestIndex;
    private int windowFirstFill = 0;
    private long position = 0;
    private int samplesLeft;
    private int batchIndex = -1;

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
        windowOldestIndex = windowSize - 1;
        readBatch();
    }

    private int baseWindowMod(int index) {
        return Math.floorMod(index, windowSize);
    }

    private void readBatch() throws IOException {
        if (batchIndex%2 == 0 || batchIndex == -1) {
            samplesLeft = computer.readBatch(evenWindow);
        } else {
            samplesLeft = computer.readBatch(oddWindow);
        }
        batchIndex++;
    }

    public int size() {
        return windowSize;
    }

    /**
     * @return the current position of the window
     */
    public long position() {
        return position;
    }

    private long positionInArray(long position) {
        return position % batchSize;
    }

    /**
     * @return true if the window is full, false otherwise
     */
    public boolean isFull() {
        return samplesLeft > 0 && windowFirstFill >= windowSize; //todo: understand why its samplesLeft > 0 and not >= 0 (probably when we decrement)
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
        position++;
        samplesLeft--;
        windowFirstFill++;
        if (samplesLeft < 0) {
            readBatch();
        }
        //System.out.println("Window index: "+ baseWindowMod(windowOldestIndex));
        //System.out.println(evenWindow[0] + " " + evenWindow[1] + " " + evenWindow[2] + " " + evenWindow[3]);
        if (batchIndex%2 == 0) {
            window[baseWindowMod(windowOldestIndex++)] = evenWindow[(int) (positionInArray(position-1))];
        } else {
            window[baseWindowMod(windowOldestIndex++)] = oddWindow[(int) (positionInArray(position-1))];
        }
        //System.out.println("Window: "+ Arrays.toString(window));
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
}