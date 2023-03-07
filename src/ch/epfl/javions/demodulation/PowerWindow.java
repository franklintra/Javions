package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @project Javions
 */

public final class PowerWindow {
    private int windowSize;
    private int[] evenWindow;
    private int[] oddWindow;

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
    }

    public int size() {
        return windowSize;
    }
}
