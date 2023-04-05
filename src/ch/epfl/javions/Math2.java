package ch.epfl.javions;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

public final class Math2 {
    private Math2() {
    } // Prevents instantiation

    /**
     * @param min   inclusive
     * @param value to clamp
     * @param max   inclusive
     * @return the clamped value
     * @throws IllegalArgumentException if the bounds are not valid.
     */
    public static int clamp(int min, int value, int max) {
        Preconditions.checkArgument(min <= max);
        return Math.max(min, Math.min(value, max));
    }

    /**
     * @param x a number
     * @return the hyperbolic arc sine of x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }
}
