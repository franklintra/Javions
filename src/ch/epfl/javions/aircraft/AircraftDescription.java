package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public record AircraftDescription(String string) {
    private static final Pattern REGEX = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * The constructor of the AircraftDescription class
     * @param string aircraft string
     * @throws IllegalArgumentException if the string is not valid
     */
    public AircraftDescription {
        Preconditions.checkArgument(REGEX.matcher(string).matches() || string.equals(""));
    }
    /**
     * @return the string of the aircraft as a string
     */
    public String string() {
        return string;
    }
}
