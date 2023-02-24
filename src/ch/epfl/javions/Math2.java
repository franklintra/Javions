package ch.epfl.javions;

/**
 * @project ${PROJECT_NAME}
 * @author @franklintra
 */

public final class Math2 {
    private Math2() {}

    /**
     * @param min inclusive
     * @param value to clamp
     * @param max inclusive
     * @return the clamped value
     * @throws IllegalArgumentException if the bounds are not valid.
     */
    public static int clamp (int min, int value, int max) throws IllegalArgumentException {
        if (min > max) {
            throw new IllegalArgumentException("Invalid bounds: min=" + min + ", max=" + max);
        }
        return Math.max(min, Math.min(value, max));
    }

    /**
     * @param x a number
     * @return the hyperbolic arc sine of x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(x*x + 1));
    }
}
