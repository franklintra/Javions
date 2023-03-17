package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {

    private final static int windowSize = 1200;
    private final PowerWindow powerWindow; // the power window used to demodulate the ADS-B messages

    /**
     * The constructor of the AdsbDemodulator class
     * @param samplesStream the stream of samples to be demodulated
     * @throws IOException if an error occurs while reading the stream
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        try {
            this.powerWindow = new PowerWindow(samplesStream, windowSize);
        } catch (IOException e) {
            throw new IOException("Error while getting the data", e);
        }
    }

    private int getBitAt(int i) {
        int p1 = powerWindow.get(80 + 10 * i); // center of first 0.5 us period
        int p2 = powerWindow.get(85 + 10 * i); // center of second 0.5 us period
        return (p1 < p2 ? 0 : 1);
    }

    private byte getByte(int i) {
        byte b = 0;
        for (int j = 0; j < 8; j++) {
            b = (byte) (b<<1);
            b = (byte) (b + getBitAt(i*8+j));
        }
        return b;
    }
    private byte[] getBytes() {
        byte[] bytes = new byte[14];
        for (int i = 0; i < 14 ; i++) {
            bytes[i] = getByte(i);
        }
        return bytes.clone();
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
        int previous = 0;
        while (powerWindow.isFull()) {
            if (sumAt(0) >= previous && sumAt(0) >= sumAt(1)) {
                int ev = powerWindow.get(5) + powerWindow.get(15) + powerWindow.get(20) + powerWindow.get(25) + powerWindow.get(30) + powerWindow.get(40);
                if (sumAt(0) >= 2*ev) {
                    if ((getBitAt(0)*16 + getBitAt(1)*8 + getBitAt(2)*4 + getBitAt(3)*2+getBitAt(4))==17) { // this checks that the df is 17
                        message = RawMessage.of(powerWindow.position()*100, getBytes());
                        if (message != null) {
                            powerWindow.advanceBy(windowSize);
                            return message;
                        }
                    }
                }
            }
            previous = sumAt(0);
            powerWindow.advance();
        }
        return message;
    }
}