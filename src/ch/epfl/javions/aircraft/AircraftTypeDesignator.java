package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public class AircraftTypeDesignator {
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9]{2,4}");
    private final String typeDesignator;

    /**
     * The constructor of the AircraftTypeDesignator class
     * @param typeDesignator ICAO aircraft type designator
     * @throws IllegalArgumentException if the type designator is not valid
     */
    public AircraftTypeDesignator(String typeDesignator) {
        Preconditions.checkArgument(REGEX.matcher(typeDesignator).matches() || typeDesignator.equals(""));
        this.typeDesignator = typeDesignator;
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

    /**
     * @param o The object to compare with
     * @return True if the two objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftTypeDesignator that = (AircraftTypeDesignator) o;
        return typeDesignator.equals(that.typeDesignator);
    }
}
