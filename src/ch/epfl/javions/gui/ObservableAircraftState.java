package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftRegistration;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

public final class ObservableAircraftState implements AircraftStateSetter {
    private final Property<IcaoAddress> icaoAddress = new SimpleObjectProperty<>();
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final Property<CallSign> callSign = new SimpleObjectProperty<>();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty velocity = new SimpleDoubleProperty();
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final Property<GeoPos> position = new SimpleObjectProperty<>();
    private final Property<ObservableList<AirbornePos>> trajectory = new SimpleObjectProperty<>(FXCollections.observableList(new ArrayList<>()));
    private final ObservableList<AirbornePos> unmodifiableTrajectory;
    private AircraftStateAccumulator<AircraftStateSetter> accumulator;
    private Property<AircraftData> aircraftData = new SimpleObjectProperty<>();

    /**
     * The constructor of the ObservableAircraftState class
     *
     * @param icaoAddress the ICAO address of the aircraft
     * @param callSign    the call sign of the aircraft
     * @param category    the category of the aircraft
     * @throws NullPointerException if the ICAO address is null
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, CallSign callSign, int category, AircraftData data) {
        Objects.requireNonNull(icaoAddress);
        this.icaoAddress.setValue(icaoAddress);
        this.callSign.setValue(callSign);
        this.category.setValue(category);
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory.getValue());
        this.accumulator = new AircraftStateAccumulator<>(this);
        this.aircraftData = new SimpleObjectProperty<>(data);
    }

    public record AirbornePos(GeoPos pos, double altitude, long timeStampNs) {
        /**
         * The constructor of the AirbornePos class
         *
         * @param pos         the position of the aircraft
         * @param altitude    the altitude of the aircraft
         * @param timeStampNs the time stamp of the last message received from the aircraft
         * @throws NullPointerException if the position is null
         */
        public AirbornePos {
            Preconditions.checkArgument(altitude >= 0);
            Preconditions.checkArgument(timeStampNs >= 0);
            Objects.requireNonNull(pos);
        }
    }

    /**
     * Updates the trajectory of the aircraft
     */

    private void updateTrajectory() {
        ObservableList<AirbornePos> currentTrajectory = getTrajectory();
        double currentAltitude = getAltitude();
        GeoPos currentPosition = getPosition();
        long currentTimestamp = getLastMessageTimeStampNs();

        if (currentTrajectory.isEmpty()) {
            currentTrajectory.add(new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
        } else {
            AirbornePos lastAirbornePos = currentTrajectory.get(currentTrajectory.size() - 1);
            long lastTimestamp = lastAirbornePos.timeStampNs();
            boolean sameTimestamp = currentTimestamp == lastTimestamp;

            if (!currentPosition.equals(currentTrajectory.get(currentTrajectory.size() - 1).pos())) {
                currentTrajectory.add(new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
            } else if (sameTimestamp) {
                currentTrajectory.set(currentTrajectory.size() - 1, new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
            }
        }
    }


    public AircraftData getAircraftData() {
        return aircraftData.getValue();
    }

    public Property<AircraftData> aircraftDataProperty() {
        return aircraftData;
    }
    public AircraftRegistration getRegistration() {
        return aircraftData.getValue().registration();
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }
    public int getCategory() {
        return category.get();
    }
    public CallSign getCallSign() {
        return callSign.getValue();
    }
    public GeoPos getPosition() {
        return position.getValue();
    }
    public double getAltitude() {
        return altitude.get();
    }
    public double getVelocity() {
        return velocity.get();
    }
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiableTrajectory;
    }


    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.setValue(callSign);
    }

    /**
     * Sets the position of the aircraft
     *
     * @param position the position of the aircraft
     * @throws NullPointerException if the position is null
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.setValue(position);
        updateTrajectory();
    }

    /**
     * Sets the altitude of the aircraft
     *
     * @param altitude the altitude of the aircraft
     * @throws IllegalArgumentException if the altitude is negative
     */
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        updateTrajectory();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyProperty<IcaoAddress> getIcaoAddress() {
        return icaoAddress;
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public ReadOnlyProperty<CallSign> callSignProperty() {
        return callSign;
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    public ReadOnlyProperty<GeoPos> positionProperty() {
        return position;
    }

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    public ReadOnlyProperty<ObservableList<AirbornePos>> trajectoryProperty() {
        return trajectory;
    }
}
