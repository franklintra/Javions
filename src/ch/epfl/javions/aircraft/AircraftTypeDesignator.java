package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */
public record AircraftTypeDesignator(String typeDesignator) {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * The constructor of the AircraftTypeDesignator class
     *
     * @param typeDesignator ICAO aircraft type designator
     * @throws IllegalArgumentException if the type designator is not valid
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument("".equals(typeDesignator) || REGEX.matcher(typeDesignator).matches());
    }

    /**
     * @return the type designator of the aircraft as a description
     */
    public String string() {
        return typeDesignator;
    }
}
