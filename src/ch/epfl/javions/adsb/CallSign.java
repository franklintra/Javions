package ch.epfl.javions.adsb;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */


public class CallSign {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 ]{0,8}");
    private final String callSign;

    /**
     * Constructs a new CallSign object with the given description.
     *
     * @param description a string describing the call sign
     * @throws IllegalArgumentException if the description is not valid according to the regex pattern
     */

    public CallSign(String description) {
        if (REGEX.matcher(description).matches() || description.equals("")) {
            this.callSign = description;
        }
        throw new IllegalArgumentException("Invalid registration: " + description);
    }

    public String string() {
        return callSign;
    }

    @Override
    public String toString() {
        return "CallSign: " + callSign;
    }
}
