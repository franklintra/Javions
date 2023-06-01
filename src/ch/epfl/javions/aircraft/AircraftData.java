package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    /**
     * Constructs a new AircraftData object with the given parameters.
     *
     * @param registration           ICAO aircraft description
     * @param typeDesignator         ICAO aircraft type designator
     * @param model                  aircraft model
     * @param description            aircraft description
     * @param wakeTurbulenceCategory aircraft wake turbulence category
     * @throws NullPointerException if any of the parameters is null
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
