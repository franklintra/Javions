package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftTypeDesignator {
    private final String typeDesignator;
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * The constructor of the AircraftTypeDesignator class
     * @param typeDesignator ICAO aircraft type designator
     */
    public AircraftTypeDesignator(String typeDesignator) {
        if (REGEX.matcher(typeDesignator).matches() || typeDesignator.equals("")) {
            this.typeDesignator = typeDesignator;
        }
        throw new IllegalArgumentException("Invalid registration: " + typeDesignator);
    }

    /**
     * @return the type designator of the aircraft as a string
     */
    public String string() {
        return typeDesignator;
    }

    /**
     * @return the type designator of the aircraft as a string formatted (default when sout-ing the object)
     */
    @Override
    public String toString() {
        return "AircraftTypeDesignator{" + typeDesignator + '}';
    }
}
