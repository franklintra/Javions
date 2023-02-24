package ch.epfl.javions;

/**
 * @project ${PROJECT_NAME}
 * @author @franklintra
 */

public class Preconditions {
    private Preconditions() {}

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
