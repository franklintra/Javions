package ch.epfl.javions.gui;/*

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

import ch.epfl.javions.adsb.*;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;

public final class AircraftStateManager {
    private final static double maxMessageAge = 60*10e9;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final Set<ObservableAircraftState> observableAircraftStates;
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.observableAircraftStates = FXCollections.observableSet(new HashSet<>());

    }

    public ObservableSet<ObservableAircraftState> getStates() {
        return (ObservableSet<ObservableAircraftState>) Collections.unmodifiableSet(observableAircraftStates);
    }

    public void updateWithMessage(RawMessage message) {
        IcaoAddress icaoAddress = message.icaoAddress();
        aircraftStateAccumulators.computeIfAbsent(icaoAddress, k-> {
            ObservableAircraftState observableAircraftState = new ObservableAircraftState(icaoAddress);
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
