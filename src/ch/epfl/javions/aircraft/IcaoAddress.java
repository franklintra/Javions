package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public record IcaoAddress(String icaoAddress) {
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress {
        Preconditions.checkArgument(REGEX.matcher(icaoAddress).matches());
    }

    /**
     * @return the ICAO description of the aircraft as a description
     */
    public String icaoAddress() {
        return icaoAddress;
    }
}
