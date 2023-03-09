package ch.epfl.javions.demodulation;


import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

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
    void testInvalidWindowSize() throws IOException {
        int windowSize = 0;
        assertThrows(IllegalArgumentException.class, () -> new PowerWindow(stream, windowSize));
    }

    @Test
    void testWindowSizeOutOfRange() throws IOException {
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

    @Test
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
    void testGetMethod() throws IOException {
        int windowSize = 3;
        PowerWindow powerWindow = new PowerWindow(stream, windowSize);

        assertEquals(1, powerWindow.get(0));
        assertEquals(2, powerWindow.get(1));
        assertEquals(3, powerWindow.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> powerWindow.get(3));
    }

    @Test
    void testAdvance() throws IOException {
        // Initialize a PowerWindow with a window size of 3
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        InputStream stream = new ByteArrayInputStream(data);
        PowerWindow window = new PowerWindow(stream, 3);

        // Verify that the window initially starts at position 0 and is not full
        assertEquals(0, window.position());
        assertFalse(window.isFull());

        // Advance the window by one sample and verify that the position is incremented
        window.advance();
        assertEquals(1, window.position());

        window.advance();
        window.advance();
        assertEquals(3, window.position());

        // Try to advance the window again and verify that an IOException is thrown because the window is full
        assertThrows(IOException.class, () -> window.advance());


    }
    @Test
    public void testAdvanceBy() throws IOException {
        // Create a mock InputStream with some sample power values
        byte[] powerValues = {10, 20, 30, 40, 50};
        ByteArrayInputStream stream = new ByteArrayInputStream(powerValues);

        // Create a PowerWindow with a window size of 3
        PowerWindow window = new PowerWindow(stream, 3);

        // Advance the window by 2 samples
        window.advanceBy(2);

        // Check that the current position of the window is 2
        assertEquals(2, window.position());

        // Check that the first sample in the window is 30
        assertEquals(30, window.get(0));

        // Check that the second sample in the window is 40
        assertEquals(40, window.get(1));

        // Check that the third sample in the window is 50
        assertEquals(50, window.get(2));

        // Advance the window by 2 more samples
        window.advanceBy(2);

        // Check that the current position of the window is 4
        assertEquals(4, window.position());

        // Check that the first sample in the window is now 50
        assertEquals(50, window.get(0));
    }
}