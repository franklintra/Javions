package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {

    private InputStream samplesStream;
    private PowerWindow powerWindow;

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.samplesStream = samplesStream;
        try {
            this.powerWindow = new PowerWindow(samplesStream, 1200);
        } catch (IOException e) {
            throw new IOException("Error while getting the data", e);
        }
    }

    public RawMessage nextMessage() throws IOException {
        return null;
    }
}