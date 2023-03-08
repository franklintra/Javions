package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @project Javions
 */

public final class PowerWindow {
    private final InputStream stream;
    private final int windowSize;
    private final int[] evenWindow;
    private final int[] oddWindow;
    private int position = 0;

    /**
     * Constructs a new PowerWindow object with the given stream and window size.
     * @param stream the stream to read from
     * @param windowSize the size of the window
     * @throws IOException if the stream cannot be read
     * @throws IllegalArgumentException if the window size is not in the range [1, 2^16]
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(0 < windowSize && windowSize <= Math.scalb(1, 16));
        this.windowSize = windowSize;
        evenWindow = new int[windowSize];
        oddWindow = new int[windowSize];
        this.stream = stream;
        prefetch(); //todo : check if this is correct
    }

    private void prefetch() throws IOException {
        for (int i = 0; i < windowSize; i++) {
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
        return position >= windowSize; // todo : check if this is correct
    }

    /**
     * @param i the position of the sample to return
     * @return the power sample at position i
     * @throws IllegalArgumentException if i is not in the range [0, windowSize - 1]
     */
    public int get(int i) {
        Preconditions.checkArgument(0 <= i && i < windowSize);
        return 1; //todo: return the power sample at position i
    }

    /**
     * Advances the window by one sample by reading the next sample from the stream.
     * @throws IOException if the stream cannot be read / if the window is full
     */
    public void advance() throws IOException {
        if (isFull()) {
            throw new IOException("Window is full");
        }
        position += 1;
    }

    /**
     * Advances the window by i samples by reading the next i samples from the stream.
     * @param i the number of samples to advance by
     * @throws IOException if the stream cannot be read / if the window is full
     * @throws IllegalArgumentException if i is negative
     */
    public void advanceBy(int i) throws IOException {
        Preconditions.checkArgument(i >= 0);
        if (isFull()) {
            throw new IOException("Window is full");
        }
        position += i;
    }
}
