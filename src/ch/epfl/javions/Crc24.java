package ch.epfl.javions;

/**
 * @author @franklintra
 * @project Javions
 */
public final class Crc24 {
    public static final int GENERATOR = 0xFFF409;
    public final int generator;
    //private final int[] TABLE;


    public Crc24(int generator) {
        this.generator = generator;
//        TABLE = buildTable(generator);
    }

//    public int crc(byte[] bytes) {
//        return crc_bitwise(bytes, generator);
//    }
public int crc(byte[] bytes) {
    int crc = 0;
    for (byte b : bytes) {
        crc ^= (b & 0xff) << 16;
        for (int i = 0; i < 8; i++) {
            crc <<= 1;
            if ((crc & 0x1000000) != 0) {
                crc ^= GENERATOR;
            }
        }
    }
    return crc & 0xffffff;
}

    private int crc_bitwise(byte[] bytes, int generator) {
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
//    private int[] buildTable() {
//        int[] table = new int[256];
//        for (int n = 0; n < 256; n++) {
//            int c = n << 16;
//            for (int k = 0; k < 8; k++) {
//                if ((c & 0x800000) != 0) {
//                    c = (c << 1) ^ generator;
//                } else {
//                    c <<= 1;
//                }
//            }
//            table[n] = c;
//        }
//        return table;
//    }

}
