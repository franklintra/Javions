package ch.epfl.javions.demodulation;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author @chukla
 * @project Javions
 */

class PowerWindowTest {
    byte[] bytes = {1, 2, 3, 4, 5};
    InputStream stream = new ByteArrayInputStream(bytes);

    @Test
    void testValidInput() throws IOException {
        int windowSize = 3;
        PowerWindow powerWindow = new PowerWindow(stream, windowSize);
        assertNotNull(powerWindow);
    }

    @Test
    void testInvalidWindowSize() {
        int windowSize = 0;
        assertThrows(IllegalArgumentException.class, () -> new PowerWindow(stream, windowSize));
    }

    @Test
    void testWindowSizeOutOfRange() {
        int windowSize = (int) Math.pow(2, 16) + 1;
        assertThrows(IllegalArgumentException.class, () -> new PowerWindow(stream, windowSize));
    }

    @Test
    void testSize() throws IOException {
        int windowSize = 5;
        PowerWindow powerWindow = new PowerWindow(stream, windowSize);
        assertEquals(windowSize, powerWindow.size());
    }

    @Test
    void testPosition() throws IOException {
        int windowSize = 3;
        PowerWindow powerWindow = new PowerWindow(stream, windowSize);

        assertEquals(0, powerWindow.position()); // position is initially 0

        powerWindow.advance();
        assertEquals(1, powerWindow.position());

        powerWindow.advance();
        assertEquals(2, powerWindow.position());

        powerWindow.advance();
        assertEquals(3, powerWindow.position());
    }

    void testIsFull() throws IOException {
        int windowSize = 3;
        PowerWindow powerWindow = new PowerWindow(stream, windowSize);

        assertFalse(powerWindow.isFull()); // window is not full initially

        powerWindow.advance();
        assertFalse(powerWindow.isFull());

        powerWindow.advance();
        assertFalse(powerWindow.isFull());

        powerWindow.advance();
        assertTrue(powerWindow.isFull());

    }

    @Test
    void testGetMethod() {
    }

    @Test
    void testAdvance() throws IOException {
        InputStream test = getClass().getResourceAsStream("/samples.bin");
        byte[] data = new byte[16];
        test.readNBytes(data, 0, 16);
        //expected := 73 292 65 745 98 4226 12244 25722 36818 23825
        int[] expected = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722};
        InputStream stream = new ByteArrayInputStream(data);
        PowerWindow window = new PowerWindow(stream, 3);

        // Verify that the window initially starts at position 0 and is not full
        assertEquals(0, window.position());
        assertFalse(window.isFull());

        // Advance the window by one sample and verify that the position is incremented
        window.advance();
        assertEquals(1, window.position());
        assertEquals(73, window.get(2));
        window.advance();
        assertEquals(292, window.get(2));
        window.advance();
        assertEquals(65, window.get(2));
        assertEquals(3, window.position());
    }

    @Test
    public void testAdvanceBy() {
    }
}