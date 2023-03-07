package ch.epfl.javions;

/**
 * @project Javions
 * @author @franklintra, @chukla
 */

public class Preconditions {
    private Preconditions() {} // Prevents instantiation

    /**
     * @param shouldBeTrue the condition to check
     * @throws IllegalArgumentException if the condition is not met
     */
    public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException("Condition not met");
        }
    }

    /**
     * @param shouldBeTrue the condition to check
     * @throws IllegalArgumentException if the condition is not met
     */
    public static void checkArgument(boolean shouldBeTrue, String message) throws IllegalArgumentException {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException(message);
        }
    }
}
