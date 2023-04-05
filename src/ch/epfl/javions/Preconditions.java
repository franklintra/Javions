package ch.epfl.javions;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

public final class Preconditions {
    private Preconditions() {
    } // Prevents instantiation

    /**
     * @param shouldBeTrue the condition to check
     * @throws IllegalArgumentException if the condition is not met
     */
    public static void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException("Condition not met");
        }
    }
}
