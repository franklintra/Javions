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
     * Constructs a new CallSign object with the given description.
     *
     * @param string a description describing the call sign
     * @throws IllegalArgumentException if the description is not valid according to the regex pattern
     */
    public CallSign {
        Preconditions.checkArgument("".equals(string) || REGEX.matcher(string).matches());
    }

    /**
     * @return the call sign
     */
    public String string() {
        return string;
    }
}
