package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public record IcaoAddress(String string) {
    /**
     * The length of an ICAO address.
     * This constant is used to check that the ICAO address is valid and to extract the ICAO address from the file more easily
     * as it is always 6 characters long.
     */
    public static final int LENGTH = 6;
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{%s}".formatted(LENGTH));

    /**
     * @param string the ICAO address
     * @throws IllegalArgumentException if the ICAO address is not valid
     */
    public IcaoAddress {
        Preconditions.checkArgument(REGEX.matcher(string).matches());
    }
}
