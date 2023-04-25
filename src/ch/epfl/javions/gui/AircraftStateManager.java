package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.*;
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
    private final static double maxMessageAge = 60*10e9;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> aircraftStateAccumulators;
    private final Set<ObservableAircraftState> observableAircraftStates;
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftStateAccumulators = new HashMap<>();
        this.observableAircraftStates = FXCollections.observableSet(new HashSet<>());
        this.aircraftDatabase = aircraftDatabase;
    }

    public ObservableSet<ObservableAircraftState> states() {
        return (ObservableSet<ObservableAircraftState>) Collections.unmodifiableSet(observableAircraftStates);
    }

    public void updateWithMessage(Message message) {
        if (message instanceof AirbornePositionMessage) {
            IcaoAddress icaoAddress = message.icaoAddress();
            aircraftStateAccumulators.computeIfAbsent(icaoAddress, k-> {
                ObservableAircraftState observableAircraftState = new ObservableAircraftState(icaoAddress, new CallSign(""), 0);
                observableAircraftStates.add(observableAircraftState);
                if (observableAircraftState.getPosition() != null) {
                    aircraftStateAccumulators.get(icaoAddress).update(message);
                }
                return new AircraftStateAccumulator<>(observableAircraftState);
            });
        }
    }

    public void purge() {
        observableAircraftStates.removeIf(state -> maxMessageAge < state.getLastMessageTimeStampNs() + System.nanoTime());
    }
}
