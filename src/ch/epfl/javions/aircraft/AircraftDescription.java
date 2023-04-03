package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public record AircraftDescription(String description) {
    private static final Pattern REGEX = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * The constructor of the AircraftDescription class
     *
     * @param description aircraft description
     * @throws IllegalArgumentException if the description is not valid
     */
    public AircraftDescription {
        Preconditions.checkArgument(REGEX.matcher(description).matches() || description.equals(""));
    }

    /**
     * @return the description of the aircraft as a description
     */
    public String string() {
        return description;
    }
}
