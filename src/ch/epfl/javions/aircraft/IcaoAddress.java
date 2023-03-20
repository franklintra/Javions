package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public record IcaoAddress(String address) {
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress {
        Preconditions.checkArgument(REGEX.matcher(address).matches());
    }

    /**
     * @return the ICAO address of the aircraft as a string
     */
    public String string() {
        return address;
    }
}
