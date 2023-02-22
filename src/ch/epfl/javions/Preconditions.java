package ch.epfl.javions;

public class Preconditions {
    private Preconditions() {}

    /**
     * @param shouldBeTrue the condition to check
     * @throws IllegalArgumentException if the condition is not met
     */
    void checkArgument(boolean shouldBeTrue) throws IllegalArgumentException {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException("Condition not met");
        }
    }
}
