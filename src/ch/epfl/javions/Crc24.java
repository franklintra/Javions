package ch.epfl.javions;

/**
 * @author @franklintra
 * @author @chukla
 * @project Javions
 */
public final class Crc24 {
    public static final int GENERATOR = 0xFFF409; // This is a default generator used to generate the CRC
    // This is the table used to optimize the algorithm (see instruction set 2.4.4). It was generated using the crc_bitwise and buildTable methods.
    private static int[] buildTable = new int[256];
    public static int generator = 0xFFF409; // This is the generator used to generate the CRC given by the user


    /**
     * This constructor builds the table for the given generator to use in the optimized algorithm
     * @param generator the generator to use
     */
    public Crc24(int generator) {
        Crc24.generator = generator;
        buildTable = buildTable();
    }

    /**
     * This returns the crc of a byte array using a table (optimized version or unoptimized according to config (optimized variable)).
     * @param bytes the bytes to calculate the crc for
     * @return the crc
     */
    public int crc(byte[] bytes) {
        //Optimized version of the bitwise algorithm using a table as described in the instruction set 2.4.4
        int crc = 0;
        for (byte b : bytes) {
            crc = (crc << 8) ^ buildTable[((crc >> 16) ^ (b & 0xff)) & 0xff];
        }
        return crc & 0xffffff;
    }

    /**
     * This returns the crc of a byte array using the bitwise algorithm. This is not optimized but is used to build the table.
     * @param bytes the bytes to calculate the crc for
     * @param generator the generator to use
     * @return the crc
     */
    private static int crc_bitwise(byte[] bytes, int generator) {
        int crc = 0;
        for (byte b : bytes) {
            crc ^= (b & 0xff) << 16;
            for (int i = 0; i < 8; i++) {
                crc <<= 1;
                if ((crc & 0x1000000) != 0) {
                    crc ^= generator;
                }
            }
        }
        return crc & 0xffffff;
    }

    /**
     * Build the table for the given generator to use in the optimized algorithm
     * @return the table
     */
    private static int[] buildTable() {
        int[] table = new int[256];
        //use crc_bitwise to build the table
        for (int i = 0; i < 256; i++) {
            table[i] = crc_bitwise(new byte[]{(byte) i}, generator);
        }
        return table;
    }
}
