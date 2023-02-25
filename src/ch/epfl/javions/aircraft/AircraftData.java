package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * @author @franklintra
 * @project Javions
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model, AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
