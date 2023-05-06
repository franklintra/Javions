package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * @author @franklintra (362694)
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
        data = bytes.clone();
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
        return Byte.toUnsignedInt(data[index]);
    }

    /**
     * @param fromIndex the index of the first byte to be returned
     * @param toIndex   the index of the last byte to be returned
     * @return the bytes in the range [fromIndex, toIndex[
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        Preconditions.checkArgument(toIndex - fromIndex < Long.BYTES);
        Objects.checkFromToIndex(fromIndex, toIndex, data.length);

        long result = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            result <<= Byte.SIZE; // moving over the last byte calculated by one to have space for the next one
            result |= byteAt(i); // appending the new byte to the end of result
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
        byte[] bytes = HEX_FORMAT.parseHex(hexString);
        return new ByteString(bytes);
    }

    /**
     * Allow to use the byte description as a key in a hash MAP for example
     *
     * @return : the hash code of the byte description
     */
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(data);
    }

    /**
     * Allow the data to be printed in hexadecimal format
     *
     * @return : the description representation of the byte description
     */
    @Override
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
        return (o instanceof ByteString) && Arrays.equals(data, ((ByteString) o).data);
    }
}