package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.*;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

public final class AircraftStateManager {
    private final AircraftDatabase aircraftDatabase;
    private final static double maxMessageAge = 6*10e9;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final ObservableSet<ObservableAircraftState> observableAircraftStates;

    /**
     * Creates a new AircraftStateManager.
     * @param aircraftDatabase the database of aircraft.
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.observableAircraftStates = FXCollections.observableSet(new HashSet<>());
        this.aircraftDatabase = aircraftDatabase;
    }

    /**
     * Returns the set of observable aircraft states.
     * @return the set of observable aircraft states.
     */
    public ObservableSet<ObservableAircraftState> states() {
        return FXCollections.unmodifiableObservableSet(observableAircraftStates);
    }

    /**
     * Updates the state of the aircraft with the given message.
     * @param message the message to update the state with.
     * @throws IOException if the aircraft database cannot be accessed.
     */
    public void updateWithMessage(Message message) throws IOException{
        IcaoAddress icaoAddress = message.icaoAddress();

        if (!aircraftStateAccumulators.containsKey(icaoAddress)) {
            AircraftData aircraftData = aircraftDatabase.get(icaoAddress);
            if (aircraftData != null) {
                ObservableAircraftState observableAircraftState = new ObservableAircraftState(icaoAddress, aircraftData);
                aircraftStateAccumulators.put(icaoAddress, new AircraftStateAccumulator<>(observableAircraftState));
            }
        } else {
            aircraftStateAccumulators.get(icaoAddress).update(message);
            if (aircraftStateAccumulators.get(icaoAddress).stateSetter().getPosition() != null) {
                observableAircraftStates.add(aircraftStateAccumulators.get(icaoAddress).stateSetter());
            }
        }
    }

    /**
     * Removes all the aircraft states that have not been updated for more than 60 seconds.
     */
    public void purge() {
        observableAircraftStates.removeIf(state -> state.getLastMessageTimeStampNs() + maxMessageAge < System.nanoTime());
    }
}
