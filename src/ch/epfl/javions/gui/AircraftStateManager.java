package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.*;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

public final class AircraftStateManager {
    private final static double maxMessageAge = 60*10e9;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final Set<ObservableAircraftState> observableAircraftStates;
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.observableAircraftStates = FXCollections.observableSet(new HashSet<>());

        // TODO: 4/10/2023 set fixed characteristics from databse
    }

    public ObservableSet<ObservableAircraftState> states() {
        return (ObservableSet<ObservableAircraftState>) Collections.unmodifiableSet(observableAircraftStates);
    }

    public void updateWithMessage(RawMessage message) {
        // TODO: 4/10/2023 check if correct 
        IcaoAddress icaoAddress = message.icaoAddress();
        aircraftStateAccumulators.computeIfAbsent(icaoAddress, k-> {
            ObservableAircraftState observableAircraftState = new ObservableAircraftState(icaoAddress, new CallSign(""), 0);
            observableAircraftStates.add(observableAircraftState);
            return new AircraftStateAccumulator<>(observableAircraftState);
        });
        Message actual = MessageParser.parse(message);
        if (actual != null) {
            aircraftStateAccumulators.get(icaoAddress).update(actual);
        }
    }

    public void purge() {
        observableAircraftStates.removeIf(state -> maxMessageAge < state.getLastMessageTimeStampNs() + System.nanoTime());
    }
}
