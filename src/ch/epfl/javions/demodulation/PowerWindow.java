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
    private final InputStream stream;
//    private final PowerComputer computer; /todo : instruction says to create powercomputer object
    private final int windowSize;
    private final int[] evenWindow;
    private final int[] oddWindow;
    private final int[] window;
    private int position = 0;

    /**
     * Constructs a new PowerWindow object with the given stream and window size.
     *
     * @param stream     the stream to read from
     * @param windowSize the size of the window
     * @throws IOException              if the stream cannot be read
     * @throws IllegalArgumentException if the window size is not in the range [1, 2^16]
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= Math.scalb(1, 16));
        this.windowSize = windowSize;
//        this.computer = new PowerComputer(stream, windowSize);
        evenWindow = new int[windowSize];
        oddWindow = new int[windowSize];
        this.stream = stream;
        window = new int[windowSize];
        prefetch();
    }

    /**
     * Prefetches the first windowSize samples from the stream.
     *
     * @throws IOException if the stream cannot be read
     */
    private void prefetch() throws IOException {
        for (int i = 0; i < windowSize; i++) { //todo : what if there are two succeeding even numbers?
            evenWindow[i] = stream.read();
            oddWindow[i] = stream.read();
        }
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

    /**
     * @return true if the window is full, false otherwise
     */
    public boolean isFull() {
        return position >= windowSize;
    }

    /**
     * @param position the position of the sample to return
     * @return the sample at the given position
     * @throws IndexOutOfBoundsException if the position is out of bounds
     */
    public int get(int position) {
        int mergedLength = evenWindow.length + oddWindow.length;
        if (position < mergedLength && position < windowSize) {
            int[] merged = new int[mergedLength];
            int i = 0;
            for (int j = 0; j < evenWindow.length; j++) {
                merged[i++] = evenWindow[j];
                if (j < oddWindow.length) {
                    merged[i++] = oddWindow[j];
                }
            }
            return merged[position + this.position];
        } else {
            throw new IndexOutOfBoundsException("Position " + position + " is out of bounds.");
        }
    }

    /**
     * Advances the window by one sample by reading the next sample from the stream.
     *
     * @throws IOException if the stream cannot be read / if the window is full
     */
    public void advance() throws IOException {
        if (isFull()) {
            throw new IOException("Window is full");
        }
        position++;
    }

   /**
     * Advances the window by the given number of samples by reading the next samples from the stream.
     *
     * @param i the number of samples to advance by
     * @throws IOException              if the stream cannot be read / if the window is full
     * @throws IllegalArgumentException if the number of samples to advance by is negative
     */
    public void advanceBy(int i) throws IOException {
        Preconditions.checkArgument(i >= 0);
        if (isFull()) {
            throw new IOException("Window is full");
        }
        position += i;
    }
}
