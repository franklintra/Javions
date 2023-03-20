package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */


public record CallSign(String string) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Constructs a new CallSign object with the given string.
     *
     * @param string a string describing the call sign
     * @throws IllegalArgumentException if the string is not valid according to the regex pattern
     */
    public CallSign {
        Preconditions.checkArgument(REGEX.matcher(string).matches() || string.equals(""));
    }

    /**
     * @return the call sign
     */

    public String string() {
        return string;
    }
}
