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
    private final Set<ObservableAircraftState> observableAircraftStates;

    /**
     * Creates a new AircraftStateManager.
     * @param aircraftDatabase the database of aircrafts.
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
        return (ObservableSet<ObservableAircraftState>) Collections.unmodifiableSet(observableAircraftStates);
    }

    /**
     * Updates the state of the aircraft with the given message.
     * @param message the message to update the state with.
     * @throws IOException if the aircraft database cannot be accessed.
     */
    public void updateWithMessage(Message message) {
        if (message instanceof AirbornePositionMessage) {
            IcaoAddress icaoAddress = message.icaoAddress();
            aircraftStateAccumulators.computeIfAbsent(icaoAddress, k-> {
                ObservableAircraftState observableAircraftState = null;
                try {
                    observableAircraftState = new ObservableAircraftState(icaoAddress, new CallSign(""), 0, aircraftDatabase.get(icaoAddress));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                observableAircraftStates.add(observableAircraftState);
                if (observableAircraftState.getPosition() != null) {
                    aircraftStateAccumulators.get(icaoAddress).update(message);
                }
                return new AircraftStateAccumulator<>(observableAircraftState);
            });
        }
    }

    /**
     * Removes all the aircraft states that have not been updated for more than 60 seconds.
     */
    public void purge() {
        observableAircraftStates.removeIf(state -> state.getLastMessageTimeStampNs() + maxMessageAge < System.nanoTime());
    }
}
