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
    public void testNextMessageWithEndOfStream() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]); // create an empty InputStream
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);

        RawMessage rawMessage = adsbDemodulator.nextMessage();
        assertNull(rawMessage);
    }

    @Test
    void testFirstFiveMessages() throws IOException {
        String[] expectedFive = new String[]{"RawMessage[timeStampNs=8096200, bytes=8D4B17E5F8210002004BB8B1F1AC]", "RawMessage[timeStampNs=75898000, bytes=8D49529958B302E6E15FA352306B]", "RawMessage[timeStampNs=100775400, bytes=8D39D300990CE72C70089058AD77]", "RawMessage[timeStampNs=116538700, bytes=8D4241A9601B32DA4367C4C3965E]", "RawMessage[timeStampNs=129268900, bytes=8D4B1A00EA0DC89E8F7C0857D5F5]"};
        String[] firstFive = new String[5];
        String f = "samples_20230304_1442.bin";
        int counter = 5;
        try (InputStream s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            for (int i = 0; i < 5; i++) {
                firstFive[i] = d.nextMessage().toString();
            }
            while ((m = d.nextMessage()) != null) {
                counter++;
                //System.out.println(m);
            }
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedFive[i], firstFive[i]);
        }
        System.out.println("nombre de message =" + counter);
    }
}