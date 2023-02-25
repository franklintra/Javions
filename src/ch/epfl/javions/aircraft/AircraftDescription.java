package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftDescription {
    private final String description;
    private static final Pattern REGEX = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    public AircraftDescription(String description) {
        if (REGEX.matcher(description).matches() || description.equals("")) {
            this.description = description;
        }
        throw new IllegalArgumentException("Invalid registration: " + description);
    }

    public String string() {
        return description;
    }
    @Override
    public String toString() {
        return "Description: " + description;
    }
}
