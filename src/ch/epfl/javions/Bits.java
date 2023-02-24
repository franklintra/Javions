package ch.epfl.javions;

/**
 * @project ${PROJECT_NAME}
 * @author @franklintra
 */

public class Bits {
    private Bits() {}

    public static int extractUInt(long value, int start, int size) {
        //FIXME: THIS IS STILL TO BE FIXED
        if (size <= 0 || size >= 32) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }
        if (start < 0 || start + size > 64) {
            throw new IndexOutOfBoundsException("Invalid range: start=" + start + ", size=" + size);
        }
        long mask = (1L << size) - 1;
        return (int) ((value >>> start) & mask);
    }
    public static boolean testBit(long value, int index) {
        if (index < 0 || index >= 64) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        return (value & (1L << index)) != 0;
    }
}
