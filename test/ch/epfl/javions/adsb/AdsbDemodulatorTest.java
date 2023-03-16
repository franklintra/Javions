package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AdsbDemodulatorTest {

    @Test
    public void testAdsbDemodulatorConstructor() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[1024]);
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);
        assertNotNull(adsbDemodulator);
    }

    @Test
    public void testAdsbDemodulatorConstructorWithNullInputStream() throws IOException {
        try {
            AdsbDemodulator adsbDemodulator = new AdsbDemodulator(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected exception
        }
    }

    @Test
    public void testAdsbDemodulatorConstructorWithInvalidInputStream() throws IOException {
        try {
            InputStream inputStream = new FileInputStream("invalidFile.txt");
            AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected exception
        }
    }

    @Test
    public void testNextMessageWithValidInputStream() throws IOException {
        byte[] samples = {}; // add valid samples here
        InputStream inputStream = new ByteArrayInputStream(samples);
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);

        RawMessage rawMessage = adsbDemodulator.nextMessage();
        assertNotNull(rawMessage);
    }


    @Test
    public void testNextMessageWithEndOfStream() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]); // create an empty InputStream
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);

        RawMessage rawMessage = adsbDemodulator.nextMessage();
        assertNull(rawMessage);
    }

}