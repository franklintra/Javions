package ch.epfl.javions.gui;/*
 * Author: Krish Chawla
 * Date:
 */
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.AircraftRegistration;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.Set;

public final class AircraftController {

    private ObservableSet<ObservableAircraftState> states = FXCollections.observableSet();

    private final ObservableSet<ObservableAircraftState> unmodifiableStates = FXCollections.unmodifiableObservableSet(states);
    private final double altitude;
    private final AircraftRegistration registration;
    private final double speed;
    private final AircraftIcon icon;
    private final ObservableList<ObservableAircraftState.AirbornePos> trajectory;


    public AircraftController(AircraftIcon icon, AircraftStateManager states, ObservableAircraftState observableAircraftState, ObjectProperty<AircraftStateSetter> selectedAircraft) {
        this.states = states.states();
        this.icon = icon;
        this.altitude = observableAircraftState.getAltitude();
        this.registration = observableAircraftState.getRegistration();
        this.speed = observableAircraftState.getVelocity();
        this.trajectory = observableAircraftState.getTrajectory();
    }

}
