package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public record IcaoAddress(String icaoAddress) {
    // This constant is used to check that the ICAO address is valid and to extract the ICAO address from the file more easily
    // as it is always 6 characters long.
    public static final int LENGTH = 6;
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{%s}".formatted(LENGTH));

    public IcaoAddress {
        Preconditions.checkArgument(REGEX.matcher(icaoAddress).matches());
    }

    /**
     * @return the ICAO description of the aircraft as a description
     */
    public String string() {
        return icaoAddress;
    }
}
