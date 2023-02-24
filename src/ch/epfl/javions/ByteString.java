package ch.epfl.javions;

import java.util.HexFormat;
import java.util.Objects;

/**
 * @project ${PROJECT_NAME}
 * @author @franklintra
 */

public final class ByteString {
    //FIXME : THIS IS STILL TO BE FIXED
    private final byte[] data;
    public ByteString(byte[] bytes) {
        this.data = bytes.clone();
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
        return data[index];
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