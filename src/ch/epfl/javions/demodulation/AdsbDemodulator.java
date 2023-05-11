package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author @franklintra (362694)
 * This class is used to demodulate ADS-B messages from a stream of samples.
 * It doesn't return specific types of messages, but rather the raw messages that it finds in the stream.
 * The demodulation is done using the power window method.
 */
public final class AdsbDemodulator {
    // The window size is the length of an ADS-B Message to be able to properly decode it
    private final static int WINDOW_SIZE = 1200;
    private final static int VALID_DOWNLINK_FORMAT = 17; // this is the Downlink Format of ADS-B messages
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
        int previousPower = 0; // this is used to compare the current power with the previous power
        while (powerWindow.isFull()) {
            if (downLinkFormat() == VALID_DOWNLINK_FORMAT) { // first check that the Downlink Format is 17
                if (sigmaP(0) >= previousPower && sigmaP(0) >= sigmaP(1)) { // this check that the current power is a local maximum
                    if (sigmaP(0) >= 2 * sigmaV()) { // this checks the necessary condition found in the ADS-B documentation
                        RawMessage message = RawMessage.of(powerWindow.position() * 100, getAllBytes());
                        if (message != null) {
                            powerWindow.advanceBy(WINDOW_SIZE);
                            return message;
                        }
                    }
                }
            }
            previousPower = sigmaP(0);
            powerWindow.advance();
        }
        return null;
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
     * The DF is calculated like this instead of (getByte(0) & 0xFF) >> 3; because of huge performance gains (~25% over all the tests)
     * It is also much faster to calculate it this way then decoding the whole message and using the message.downLinkFormat method (2s gains out of 4s)
     *
     * @return the DF of the ADS-B message
     */
    private int downLinkFormat() {
        return getBitAt(0) * 16 + getBitAt(1) * 8 + getBitAt(2) * 4 + getBitAt(3) * 2 + getBitAt(4); //this is the DF of the message.
    }

    /**
     * This method returns the i-th bit that can be decoded from the ADS-B message
     * The 80 + 10i and 85 + 10i come from the instruction set. As they are only used in this method we chose not to implement class Constants
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
        for (int j = 0; j < Byte.SIZE; j++) {
            b = (byte) (b << 1);
            b = (byte) (b | getBitAt(i * 8 + j));
        }
        return b;
    }

    /**
     * This method returns all the bytes that can be decoded from the ADS-B message
     *
     * @return all the bytes that can be decoded from the ADS-B message
     */
    private byte[] getAllBytes() {
        byte[] bytes = new byte[RawMessage.LENGTH];
        for (int i = 0; i < RawMessage.LENGTH; i++) {
            bytes[i] = getByte(i);
        }
        return bytes;
    }
}