package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unused")
class BitsTest {
    @Test
    void bitsExtractUIntThrowsIfSizeIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Bits.extractUInt(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> Bits.extractUInt(0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Bits.extractUInt(0, 0, 32));
    }

    @Test
    void bitsExtractUIntThrowsIfStartAndSizeAreInvalid() {
        assertThrows(IndexOutOfBoundsException.class, () -> Bits.extractUInt(0, -1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> Bits.extractUInt(0, 64, 1));
    }

    @Test
    void bitsExtractUIntCanExtractAllNibbles() {
        var v = 0xFEDCBA9876543210L;
        for (var i = 0; i < 16; i += 1) {
            var n = Bits.extractUInt(v, i * 4, 4);
            assertEquals(i, n);
        }
    }

    @Test
    void bitsExtractCanExtract31Bits() {
        var v = 0xFFFF_7654_ABCD_FFFFL;
        var n = Bits.extractUInt(v, 16, 31);
        assertEquals(0x7654_ABCD, n);
    }

    @Test
    void understandingHowBitsExtractUIntWorks() {
        // this method extracts n bits so that the lowest weight bit of the result is the bit at index start (inclusive)
        // for example Bits.extractUInt(0b101101110, 1, 5) returns 0b10111
        // and Bits.extractUInt(0b101101110, 0, 5) returns 0b 1110
        long a = 0b101101110;
        assertEquals(0b1110, Bits.extractUInt(a, 0, 5));
        assertEquals(0b10111, Bits.extractUInt(a, 1, 5));
    }

    @Test
    void bitsTestThrowsIfIndexIsInvalid() {
        assertThrows(IndexOutOfBoundsException.class, () -> Bits.testBit(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> Bits.testBit(0, Long.SIZE));
    }

    @Test
    void bitsTestBitWorksOnAllBits() {
        for (var i = 0; i < Long.SIZE; i += 1) {
            var v = 1L << i;
            for (var j = 0; j < Long.SIZE; j += 1) {
                var b = Bits.testBit(v, j);
                assertEquals(i == j, b);
            }
        }
    }
}