package ch.epfl.javions;

/**
 * @author @franklintra
 * @project Javions
 */
public class Crc24 {
    public final static int GENERATOR = 0xFFF409;
    private final static int[] TABLE = buildTable();

    public Crc24(int generator) {
    }

    public static int crc(byte[] bytes) {
        return crc_bitwise(bytes, GENERATOR);
    }

    private static int crc_bitwise(byte[] bytes, int generator) {
        int crc = 0;
        for (byte b : bytes) {
            crc  = (crc << 1) | b;
            // if the 24-th bit from the right is 1, xor with the generator
            if ((crc & 0x1000000) != 0) {
                crc ^= generator;
            }
        }
        return crc & 0xFFFFFF;
    }

    /**
     * Builds a table of 256 24-bit CRCs of all possible 8-bit bytes.
     */
    private static int[] buildTable() {
        int[] table = new int[256];
        for (int n = 0; n < 256; n++) {
            int c = n << 16;
            for (int k = 0; k < 8; k++) {
                if ((c & 0x800000) != 0) {
                    c = (c << 1) ^ GENERATOR;
                } else {
                    c <<= 1;
                }
            }
            table[n] = c;
        }
        return table;
    }
}
