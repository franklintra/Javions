package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */


public class CallSign {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 ]{0,8}");
    private final String callSign;

    /**
     * Constructs a new CallSign object with the given callSign.
     *
     * @param callSign a string describing the call sign
     * @throws IllegalArgumentException if the callSign is not valid according to the regex pattern
     */

    public CallSign(String callSign) {
        Preconditions.checkArgument(REGEX.matcher(callSign).matches() || callSign.equals(""));
        this.callSign = callSign;
    }

    /**
     * @return the call sign
     */

    public String string() {
        return callSign;
    }

    @Override
    public String toString() {
        return "CallSign: " + callSign;
    }
}
