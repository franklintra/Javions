package ch.epfl.javions;

/**
 * @author @franklintra (362694)
 * @project ${PROJECT_NAME}
 */

public final class Bits {
    private Bits() {
    } // Prevents instantiation

    /**
     * @param value the value to extract from
     * @param start the start index (inclusive)
     * @param size  the size of the range
     * @return the extracted value
     * @throws IllegalArgumentException  if the size is invalid
     * @throws IndexOutOfBoundsException if the range is invalid
     */
    public static int extractUInt(long value, int start, int size) {
        Preconditions.checkArgument(0 < size && size < Integer.SIZE);
        if (start < 0 || start + size > Long.SIZE) {
            throw new IndexOutOfBoundsException("Invalid range: start=" + start + ", size=" + size);
        }
        final long mask = (1L << size) - 1;
        return (int) ((value >> start) & mask);
    }

    /**
     * Returns the value of the bit at the specified index in the given long value.
     *
     * @param value the long value to test
     * @param index the index of the bit to test
     * @return true if the bit is set, false otherwise
     * @throws IndexOutOfBoundsException if the index is negative or greater than or equal to 64
     */
    public static boolean testBit(long value, int index) {
        if (index < 0 || index >= Long.SIZE) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        return (value & (1L << index)) != 0;
    }
}
