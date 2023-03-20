package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public record AircraftRegistration(String registration) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * The constructor of the AircraftRegistration class
     * @param registration ICAO aircraft registration
     * @throws IllegalArgumentException if the registration is not valid
     */
    public AircraftRegistration {
        Preconditions.checkArgument(REGEX.matcher(registration).matches());
    }

    /**
     * @return the registration of the aircraft as a string
     */
    public String string() {
        return registration;
    }
}