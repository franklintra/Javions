package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class IcaoAddress {
    private final String address;
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress(String address) {
        if (REGEX.matcher(address).matches()) {
            this.address = address;
        }
        throw new IllegalArgumentException("Invalid ICAO address: " + address);
    }

    public String string() {
        return address;
    }

    @Override
    public String toString() {
        return "ICAO address: " + address;
    }
}
