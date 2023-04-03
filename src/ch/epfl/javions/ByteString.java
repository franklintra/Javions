package ch.epfl.javions;

import java.util.HexFormat;
import java.util.Objects;

/**
 * @author @franklintra
 * @project Javions
 */

public final class ByteString {
    private final byte[] data;
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    /**
     * The constructor of the ByteString class
     *
     * @param bytes the bytes to be stored in the byte description
     */
    public ByteString(byte[] bytes) {
        data = new byte[bytes.length];
        System.arraycopy(bytes, 0, data, 0, bytes.length); // This is to make sure that the byte array is immutable (the array is copied)
    }

    /**
     * @return the size of the byte description
     */
    public int size() {
        return data.length;
    }

    /**
     * @param index the index of the byte to be returned
     * @return the byte at the given index
     */
    public int byteAt(int index) {
        Objects.checkIndex(index, data.length);
        return data[index] & 0xFF; // This is to make sure that the byte is unsigned
    }

    /**
     * @param fromIndex the index of the first byte to be returned
     * @param toIndex   the index of the last byte to be returned
     * @return the bytes in the range [fromIndex, toIndex[
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Preconditions.checkArgument(toIndex - fromIndex < 8);
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

    /**
     * @param hexString the hex description to be converted to a byte description
     * @return the byte description corresponding to the hex description
     * @throws IllegalArgumentException if the hex description is not valid
     */
    public static ByteString ofHexadecimalString(String hexString) {
        Preconditions.checkArgument(hexString.length() % 2 == 0);
        if (!hexString.matches("[0-9a-fA-F]+")) {
            throw new NumberFormatException("Only hexadecimal characters are allowed");
        }
        byte[] bytes = HEX_FORMAT.parseHex(hexString);
        return new ByteString(bytes);
    }

    /**
     * Allow to use the byte description as a key in a hash map for example
     *
     * @return : the hash code of the byte description
     */
    public int hashCode() {
        return java.util.Arrays.hashCode(data);
    }

    /**
     * Allow the data to be printed in hexadecimal format
     *
     * @return : the description representation of the byte description
     */
    public String toString() {
        return HEX_FORMAT.formatHex(data);
    }

    /**
     * Does a structural comparison of the two byte strings.
     *
     * @param o: the object to compare to
     * @return : true if the two byte strings are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ByteString)) {
            return false;
        }
        return java.util.Arrays.equals(data, ((ByteString) o).data);
    }
}