package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */
public record AircraftRegistration(String string) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * The constructor of the AircraftRegistration class
     *
     * @param string ICAO aircraft description
     * @throws IllegalArgumentException if the description is not valid
     */
    public AircraftRegistration {
        Preconditions.checkArgument(REGEX.matcher(string).matches());
    }
}