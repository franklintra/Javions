package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public record AircraftRegistration(String string) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * The constructor of the AircraftRegistration class
     * @param string ICAO aircraft string
     * @throws IllegalArgumentException if the string is not valid
     */
    public AircraftRegistration {
        Preconditions.checkArgument(REGEX.matcher(string).matches());
    }

    /**
     * @return the string of the aircraft as a string
     */
    public String string() {
        return string;
    }
}