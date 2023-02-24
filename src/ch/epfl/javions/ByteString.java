package ch.epfl.javions;

import java.util.HexFormat;
import java.util.Objects;

/**
 * @project Javions
 * @author @franklintra
 */

public final class ByteString {
    private final byte[] data;
    public ByteString(byte[] bytes) {
        this.data = bytes.clone();
    }

    /**
     * Does a structural comparison of the two byte strings.
     * @param o: the object to compare to
     * @return : true if the two byte strings are equal, false otherwise
     */
    public boolean equals(Object o) {
        if (!(o instanceof ByteString)) {
            return false;
        }
        return java.util.Arrays.equals(data, ((ByteString) o).data);
    }

    /**
     * Allow the data to be printed in hexadecimal format
     * @return : the string representation of the byte string
     */
    public String toString() {
        HexFormat hf = HexFormat.of().withUpperCase();
        return hf.formatHex(data);
    }

    /**
     * Allow to use the byte string as a key in a hash map for example
     * @return : the hash code of the byte string
     */
    public int hashCode() {
        return java.util.Arrays.hashCode(data);
    }

    public static ByteString ofHexadecimalString(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }
        if (!hexString.matches("[0-9a-fA-F]+")) {
            throw new NumberFormatException("Only hexadecimal characters are allowed");
        }
        HexFormat hf = HexFormat.of().withUpperCase();
        byte[] bytes = hf.parseHex(hexString);
        return new ByteString(bytes);
    }
    public int size() {
        return data.length;
    }
    public int byteAt(int index) {
        Objects.checkIndex(index, data.length);
        return data[index] & 0xFF; // This is to make sure that the byte is unsigned
    }
    public long bytesInRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, data.length);
        byte[] bytes = new byte[toIndex - fromIndex];
        System.arraycopy(data, fromIndex, bytes, 0, toIndex - fromIndex);
        long result = 0;
        for (byte b : bytes) {
            result <<= 8;
            result |= b & 0xFF;
        }
        return result;
    }
}