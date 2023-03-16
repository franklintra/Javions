package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

public final class AdsbDemodulator {

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
            this.powerWindow = new PowerWindow(samplesStream, 1200);
        } catch (IOException e) {
            throw new IOException("Error while getting the data", e);
        }
    }

    private ByteString getBits() {
        short[] window = new short[1200];
        for (int i = 0; i < 1200; i++) {
            window[i] = (short) powerWindow.get(i);
        }
        //convert window to a hex string
        byte[] bytes = new byte[window.length*2];
        for (int i = 0; i < window.length; i++) {
            bytes[2*i] = (byte) (window[i] >> 8);
            bytes[2*i+1] = (byte) window[i];
        }
        System.out.println(ByteString.ofHexadecimalString(HexFormat.of().formatHex(bytes).toUpperCase()).toString());
        return ByteString.ofHexadecimalString(HexFormat.of().formatHex(bytes).toUpperCase());
    }

    /**
     * This method returns the next ADS-B message in the stream.
     * @return the next ADS-B message in the stream
     * @throws IOException if an error occurs while reading the stream
     */
    public RawMessage nextMessage() throws IOException {
        RawMessage message = null;
        while (true) {
            if (powerWindow.get(0)+powerWindow.get(10)+powerWindow.get(35)+powerWindow.get(45) > 2*(powerWindow.get(5)+powerWindow.get(15)+ powerWindow.get(20)+powerWindow.get(25)+powerWindow.get(30)+powerWindow.get(40))) {
                message = new RawMessage(1L, getBits());
                powerWindow.advance();
                return message;
            }
            else {
                powerWindow.advance();
            }
        }
    }
}