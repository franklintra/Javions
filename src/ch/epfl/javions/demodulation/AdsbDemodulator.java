package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to demodulate ADS-B messages from a stream of samples.
 * It doesn't return specific types of messages, but rather the raw messages that it finds in the stream.
 * The demodulation is done using the power window method.
 */
public final class AdsbDemodulator {
    // The window size is the length of an ADS-B Message to be able to properly decode it
    private final static int WINDOW_SIZE = 1200;
    private final PowerWindow powerWindow; // the power window used to demodulate the ADS-B messages

    /**
     * The constructor of the AdsbDemodulator class
     *
     * @param samplesStream the stream of samples to be demodulated
     * @throws IOException if an error occurs while reading the stream
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.powerWindow = new PowerWindow(samplesStream, WINDOW_SIZE);
    }


    /**
     * This method returns the next ADS-B message in the stream.
     *
     * @return the next ADS-B message in the stream
     * @throws IOException if an error occurs while reading the stream
     */
    public RawMessage nextMessage() throws IOException {
        RawMessage message = null;
        int previous = 0; // this is used to compare the current power with the previous power
        while (powerWindow.isFull()) {
            if (sigmaP(0) >= previous && sigmaP(0) >= sigmaP(1)) { // this check that the current power is a local maximum
                if (sigmaP(0) >= 2 * sigmaV()) { // this checks the necessary condition found in the ADS-B documentation
                    if ((getBitAt(0) * 16 + getBitAt(1) * 8 + getBitAt(2) * 4 + getBitAt(3) * 2 + getBitAt(4)) == 17) { // this checks that the df is 17
                        message = RawMessage.of(powerWindow.position() * 100, getAllBytes());
                        if (message != null) {
                            powerWindow.advanceBy(WINDOW_SIZE);
                            return message;
                        }
                    }
                }
            }
            previous = sigmaP(0);
            powerWindow.advance();
        }
        return message;
    }

    /**
     * This method returns the sum Sigma P of the powerWindow offset by i
     *
     * @param i the offset to calculate the sum at
     * @return the sum Sigma P of the powerWindow offset by i
     */
    private int sigmaP(int i) {
        return powerWindow.get(i) + powerWindow.get(10 + i) + powerWindow.get(35 + i) + powerWindow.get(45 + i);
    }

    /**
     * @return the sum Sigma V of the powerWindow.
     */
    private int sigmaV() {
        return powerWindow.get(5) + powerWindow.get(15) + powerWindow.get(20) + powerWindow.get(25) + powerWindow.get(30) + powerWindow.get(40);
    }

    /**
     * This method returns the i-th bit that can be decoded from the ADS-B message
     *
     * @param i the index of the bit
     * @return the i-th bit that can be decoded from the ADS-B message
     */
    private int getBitAt(int i) {
        int p1 = powerWindow.get(80 + 10 * i); // center of first 0.5 us period
        int p2 = powerWindow.get(85 + 10 * i); // center of second 0.5 us period
        return (p1 < p2 ? 0 : 1);
    }

    /**
     * This method returns the i-th byte that can be decoded from the ADS-B message
     *
     * @param i the index of the byte
     * @return the i-th byte that can be decoded from the ADS-B message
     */
    private byte getByte(int i) {
        byte b = 0;
        for (int j = 0; j < 8; j++) {
            b = (byte) (b << 1);
            b = (byte) (b + getBitAt(i * 8 + j));
        }
        return b;
    }

    /**
     * This method returns all the bytes that can be decoded from the ADS-B message
     *
     * @return all the bytes that can be decoded from the ADS-B message
     */
    private byte[] getAllBytes() {
        byte[] bytes = new byte[14];
        for (int i = 0; i < 14; i++) {
            bytes[i] = getByte(i);
        }
        return bytes;
    }
}