package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 * This class is responsible for managing the state of the aircrafts.
 * It links the aircrafts to their state accumulators.
 * It also removes the aircrafts that have not been updated for more than 60 seconds.
 * It also gives the unmodifiable observable set of aircraft states that is used by JavaFX.
 */
public final class AircraftStateManager {
    private final static double maxMessageAge = 6 * 1e9;
    private final AircraftDatabase database;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final ObservableSet<ObservableAircraftState> observableModifiableAircraftStates = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<ObservableAircraftState> observableUnmodifiableAircraftStates = FXCollections.unmodifiableObservableSet(observableModifiableAircraftStates);
    private long lastTimeStampNs;


    /**
     * Creates a new AircraftStateManager.
     *
     * @param aircraftDatabase the database of aircraft.
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.database = aircraftDatabase;
    }

    /**
     * Returns the set of observable aircraft states.
     *
     * @return the set of observable aircraft states.
     */
    public ObservableSet<ObservableAircraftState> states() {
        return observableUnmodifiableAircraftStates;
    }

    /**
     * Updates the state of the aircraft with the given message.
     *
     * @param message the message to update the state with.
     */
    public void updateWithMessage(Message message) {
        lastTimeStampNs = message.timeStampNs();
        IcaoAddress icaoAddress = message.icaoAddress();
        if (!aircraftStateAccumulators.containsKey(icaoAddress)) { // If the aircraft is not yet in the state accumulator
            // In case the aircraft is not in the database, we do not add it to the observableAircraftStates
            // If the aircraft is in the database, we add it to the aicraftStateAccumulators if it wasn't already
            AircraftData data;
            try {
                data = database.get(icaoAddress);
                if (data == null) {
                    return; // Do not add the aircraft to the observableAircraftStates (it is not in the database) todo
                }
            } catch (IOException e) {
                return; // todo check this behaviour
            }

            ObservableAircraftState aircraftState = new ObservableAircraftState(icaoAddress, data);
            aircraftStateAccumulators.put(icaoAddress, new AircraftStateAccumulator<>(aircraftState));
        }

        // We only add the aircraft to the observableAircraftStates if it has a position
        if (aircraftStateAccumulators.get(icaoAddress).stateSetter().getPosition() != null) {
            observableModifiableAircraftStates.add(aircraftStateAccumulators.get(icaoAddress).stateSetter());
        }

        aircraftStateAccumulators.get(icaoAddress).update(message);
    }

    /**
     * Removes all the aircraft states that have not been updated for more than 60 seconds.
     */
    public void purge() {
        observableModifiableAircraftStates.removeIf(state -> {
            if (lastTimeStampNs - state.getLastMessageTimeStampNs() > maxMessageAge) {
                // If the last message is too old: Remove the aircraft from the accumulator as well
                aircraftStateAccumulators.remove(state.getIcaoAddress());
                return true; // Remove the aircraft from the observableAircraftStates
            }
            return false; // Do not remove the aircraft from the observableAircraftStates (it is still active)
        });
    }
}
