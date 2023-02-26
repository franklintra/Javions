package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftRegistration {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 .?/_+-]+");
    private final String registration;

    /**
     * The constructor of the AircraftRegistration class
     * @param registration ICAO aircraft registration
     */
    public AircraftRegistration(String registration) {
        if (REGEX.matcher(registration).matches()) {
            this.registration = registration;
        }
        throw new IllegalArgumentException("Invalid registration: " + registration);
    }

    /**
     * @return the registration of the aircraft as a string
     */
    public String string() {
        return registration;
    }

    /**
     * @return the registration of the aircraft as a string formatted (default when sout-ing the object)
     */
    @Override
    public String toString() {
        return "Registration{" + registration + '}';
    }
}
