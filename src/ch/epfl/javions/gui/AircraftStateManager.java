package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 * The AircraftStateManager class is responsible for managing the state of aircraft.
 * It links the aircraft to their state accumulators, removes aircraft that have not been updated for more than 60 seconds,
 * and provides an unmodifiable observable set of aircraft states that is used by JavaFX.
 * <p>
 * This class is used to update and track the states of multiple aircraft based on received messages.
 */
public final class AircraftStateManager {
    // todo : check the comments on this class @chukla @issue
    public final static double maxMessageAge = 60 * 1e9;
    private final AircraftDatabase database;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final ObservableSet<ObservableAircraftState> aircraftStates = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<ObservableAircraftState> observableUnmodifiableAircraftStates = FXCollections.unmodifiableObservableSet(aircraftStates);
    private long lastTimeStampNs;

    /**
     * Creates a new AircraftStateManager with the specified aircraft database.
     *
     * @param aircraftDatabase the database of aircraft
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.database = aircraftDatabase;
    }

    /**
     * Returns the set of observable aircraft states.
     *
     * @return the set of observable aircraft states
     */
    public ObservableSet<ObservableAircraftState> states() {
        return observableUnmodifiableAircraftStates;
    }

    /**
     * Updates the state of the aircraft with the given message
     * and creates them when the first message is received.
     *
     * @param message the message to update the state with
     */
    public void updateWithMessage(Message message) throws IOException {
        lastTimeStampNs = message.timeStampNs();
        IcaoAddress icaoAddress = message.icaoAddress();

        aircraftStateAccumulators.putIfAbsent(icaoAddress,
                new AircraftStateAccumulator<>(
                        new ObservableAircraftState(icaoAddress, database.get(icaoAddress)))
        );
        aircraftStateAccumulators.get(icaoAddress).update(message);

        // We only add the aircraft to the observableAircraftStates if it has a known position
        if (aircraftStateAccumulators.get(icaoAddress).stateSetter().getPosition() != null) {
            aircraftStates.add(aircraftStateAccumulators.get(icaoAddress).stateSetter());
        }
    }

    /**
     * Removes all aircraft states that have not been updated for more than 60 seconds.
     * This method is called in the AnimationTimer of the main JavaFX thread.
     *
     * @see Main#start(Stage)
     */
    public void purge() {
        aircraftStates.removeIf(state -> {
            if (lastTimeStampNs - state.getLastMessageTimeStampNs() > maxMessageAge) {
                // If the last message is too old: Remove the aircraft from the accumulator as well
                aircraftStateAccumulators.remove(state.getIcaoAddress());
                return true; // Remove the aircraft from the observableAircraftStates
            }
            return false; // Do not remove the aircraft from the observableAircraftStates (it is still active)
        });
    }
}
