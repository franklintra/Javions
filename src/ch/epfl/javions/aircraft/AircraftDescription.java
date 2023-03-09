package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftDescription {
    private static final Pattern REGEX = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");
    private final String description;

    /**
     * The constructor of the AircraftDescription class
     * @param description aircraft description
     * @throws IllegalArgumentException if the description is not valid
     */
    public AircraftDescription(String description) {
        Preconditions.checkArgument(REGEX.matcher(description).matches() || description.equals(""));
        this.description = description;
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

    /**
     * @param o The object to compare with
     * @return True if the two objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftDescription that = (AircraftDescription) o;
        return description.equals(that.description);
    }
    }
