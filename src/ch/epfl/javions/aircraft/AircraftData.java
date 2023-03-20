package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @author @franklintra, @chukla
 * @project Javions
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model, AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * @param registration ICAO aircraft string
     * @param typeDesignator ICAO aircraft type designator
     * @param model aircraft model
     * @param description aircraft string
     * @param wakeTurbulenceCategory aircraft wake turbulence category
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
