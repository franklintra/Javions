package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

public final class AdsbDemodulator {

    private final static int windowSize = 1200;
    private final InputStream samplesStream; // the stream of samples to be demodulated
    private final PowerWindow powerWindow; // the power window used to demodulate the ADS-B messages

    /**
     * The constructor of the AdsbDemodulator class
     * @param samplesStream the stream of samples to be demodulated
     * @throws IOException if an error occurs while reading the stream
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.samplesStream = samplesStream;
        try {
            this.powerWindow = new PowerWindow(samplesStream, windowSize);
        } catch (IOException e) {
            throw new IOException("Error while getting the data", e);
        }
    }

    private ByteString getBits() {
        return null;
    }

    private boolean getBitAt(long position) {
        return false; // todo: return the bit at a position. to be used in the next method
    }

    private byte[] getBytes() {
        return null; // todo: give the bytes from the current window
    }

    private int sumAt(int i) {
        return powerWindow.get(i)+powerWindow.get(10 + i)+powerWindow.get(35 + i)+powerWindow.get(45 + i);
    }

    /**
     * This method returns the next ADS-B message in the stream.
     * @return the next ADS-B message in the stream
     * @throws IOException if an error occurs while reading the stream
     */
    public RawMessage nextMessage() throws IOException {
        RawMessage message = null;
        while (powerWindow.isFull()) {
            if (sumAt(0) >= sumAt(-1) && sumAt(0) >= sumAt(1)) { //  superieur a +1 et -1
                int ev = powerWindow.get(5) + powerWindow.get(15) + powerWindow.get(20) + powerWindow.get(25) + powerWindow.get(30) + powerWindow.get(40);
                if ( sumAt(0) >= 2*ev) {
                    if (false) { // todo : check if the df is 17
                        //decodage
                        // decode and return everything
                        powerWindow.advanceBy(windowSize);
                    }
                }
            }
            powerWindow.advance();
        }
        return null;
    }
}