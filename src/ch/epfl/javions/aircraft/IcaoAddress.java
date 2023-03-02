package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class IcaoAddress {
    private static final Pattern REGEX = Pattern.compile("[0-9A-F]{6}");
    private final String address;

    /**
     * The constructor of the IcaoAddress class
     * @param address ICAO address
     */
    public IcaoAddress(String address) {
        Preconditions.checkArgument(REGEX.matcher(address).matches());
        this.address = address;
    }

    /**
     * @return the ICAO address of the aircraft as a string
     */
    public String string() {
        return address;
    }

    /**
     * @return the ICAO address of the aircraft as a string formatted (default when sout-ing the object)
     */
    @Override
    public String toString() {
        return "ICAO address: " + address;
    }
}
