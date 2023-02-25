package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftTypeDesignator {
    private final String typeDesignator;
    private static final Pattern REGEX = Pattern.compile("[A-Z0-9]{2,4}");

    public AircraftTypeDesignator(String typeDesignator) {
        if (REGEX.matcher(typeDesignator).matches() || typeDesignator.equals("")) {
            this.typeDesignator = typeDesignator;
        }
        throw new IllegalArgumentException("Invalid registration: " + typeDesignator);
    }
    public String string() {
        return typeDesignator;
    }
    @Override
    public String toString() {
        return "AircraftTypeDesignator{" + typeDesignator + '}';
    }
}
