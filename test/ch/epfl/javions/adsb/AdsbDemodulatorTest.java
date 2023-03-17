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
    public void testAdsbDemodulatorConstructorWithNullInputStream() {
        assertThrows(NullPointerException.class, () -> new AdsbDemodulator(null));
    }

    @Test
    public void testAdsbDemodulatorConstructorWithInvalidInputStream() {
        assertThrows(IOException.class, () -> new AdsbDemodulator(new FileInputStream("invalidFile.txt")));
    }


    @Test
    public void testNextMessageWithEndOfStream() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]); // create an empty InputStream
        AdsbDemodulator adsbDemodulator = new AdsbDemodulator(inputStream);
        RawMessage rawMessage = adsbDemodulator.nextMessage();
        assertNull(rawMessage);
    }

    @Test
    public void testNextMessageWithNullInput() throws IOException {
        assertThrows(IOException.class, () -> {
            AdsbDemodulator adsbDemodulator = new AdsbDemodulator(new FileInputStream("invalidFile.txt"));
            new AdsbDemodulator(new FileInputStream("invalidFile.txt"));
        });
    }



    @Test
    void testFirstFiveMessages() throws IOException {
        String[] expectedFive = new String[]{"RawMessage[timeStampNs=8096200, bytes=8D4B17E5F8210002004BB8B1F1AC]", "RawMessage[timeStampNs=75898000, bytes=8D49529958B302E6E15FA352306B]", "RawMessage[timeStampNs=100775400, bytes=8D39D300990CE72C70089058AD77]", "RawMessage[timeStampNs=116538700, bytes=8D4241A9601B32DA4367C4C3965E]", "RawMessage[timeStampNs=129268900, bytes=8D4B1A00EA0DC89E8F7C0857D5F5]"};
        String[] firstFive = new String[5];
        int messageAmount = 0;
        try (InputStream s = getClass().getResourceAsStream("/samples_20230304_1442.bin")) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if (messageAmount < 5) {
                    firstFive[messageAmount] = m.toString();
                }
                messageAmount++;
            }
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedFive[i], firstFive[i]);
        }
    }
}