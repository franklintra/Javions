package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftRegistration {
    private final String registration;
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9 .?/_+-]+");

    public AircraftRegistration(String registration) {
        if (REGEX.matcher(registration).matches()) {
            this.registration = registration;
        }
        throw new IllegalArgumentException("Invalid registration: " + registration);
    }

    @Override
    public String toString() {
        return "Registration{" + registration + '}';
    }
}
