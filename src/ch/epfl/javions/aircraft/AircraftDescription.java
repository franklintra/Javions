package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftDescription {
    private final String description;
    private static final Pattern REGEX = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * The constructor of the AircraftDescription class
     * @param description aircraft description
     */
    public AircraftDescription(String description) {
        if (REGEX.matcher(description).matches() || description.equals("")) {
            this.description = description;
        }
        throw new IllegalArgumentException("Invalid registration: " + description);
    }
    /**
     * @return the description of the aircraft as a string
     */
    public String string() {
        return description;
    }

    /**
     * @return the description of the aircraft as a string formatted (default when sout-ing the object)
     */
    @Override
    public String toString() {
        return "Description: " + description;
    }
}
