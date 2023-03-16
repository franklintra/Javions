package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

public final class AdsbDemodulator {

    private static int windowSize = 1200;
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

    /**
     * This method returns the next ADS-B message in the stream.
     * @return the next ADS-B message in the stream
     * @throws IOException if an error occurs while reading the stream
     */
    public RawMessage nextMessage() throws IOException {
        RawMessage message = null;
        int[] sums = new int[3];
        int index = -1;
        while (true) {
            index++;
            sums[index % 3] = powerWindow.get(0)+powerWindow.get(10)+powerWindow.get(35)+powerWindow.get(45);
            if (sums[index%3] <= sums[(index+1)%3] || sums[index%3] <= sums[(index+2)%3]) {
                powerWindow.advanceBy(windowSize);
                continue;
            }
            if (sums[index] > 2*(powerWindow.get(5)+powerWindow.get(15)+ powerWindow.get(20)+powerWindow.get(25)+powerWindow.get(30)+powerWindow.get(40))) {
                powerWindow.advanceBy(2); // this is to go forward 1/4 microsecond
                message = new RawMessage(1L, getBits());
                powerWindow.advanceBy(10); // fixme : advance by message bits (i don't remember how long it is)
                return message;
            }
            else {
                powerWindow.advance();
            }
        }
    }
}