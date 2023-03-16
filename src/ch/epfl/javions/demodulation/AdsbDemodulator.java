package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author @franklintra
 * @project Javions
 */
public final class AdsbDemodulator {
    private PowerWindow window;
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        try {
            window = new PowerWindow(samplesStream, 1200);
        } catch (IOException e) {
            throw new IOException("Error while reading the samples stream", e);
        }
    }

    public RawMessage nextMessage() {
        return null;
    }
}
