package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public record AircraftTypeDesignator(String string) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * The constructor of the AircraftTypeDesignator class
     * @param string ICAO aircraft type designator
     * @throws IllegalArgumentException if the type designator is not valid
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(REGEX.matcher(string).matches() || string.equals(""));
    }

    /**
     * @return the type designator of the aircraft as a string
     */
    public String string() {
        return string;
    }
}
