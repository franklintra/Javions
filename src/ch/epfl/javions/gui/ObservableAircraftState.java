package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */


public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final Property<CallSign> callSign = new SimpleObjectProperty<>();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty velocity = new SimpleDoubleProperty();
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final Property<GeoPos> position = new SimpleObjectProperty<>();
    private final ObservableList<AirbornePos> trajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    private final AircraftStateAccumulator<AircraftStateSetter> accumulator;
    private long previousTimestamp;

    /**
     * The constructor of the ObservableAircraftState class
     *
     * @param icaoAddress the ICAO address of the aircraft
     * @throws NullPointerException if the ICAO address is null
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData data) {
        Objects.requireNonNull(icaoAddress);
        this.icaoAddress = icaoAddress;
        this.aircraftData = data;
        this.accumulator = new AircraftStateAccumulator<>(this);
    }

    public record AirbornePos(GeoPos pos, double altitude) {
        /**
         * The constructor of the AirbornePos class
         *
         * @param pos         the position of the aircraft
         * @param altitude    the altitude of the aircraft
         * @throws NullPointerException if the position is null
         */
        public AirbornePos {
            Preconditions.checkArgument(altitude >= 0);
        }
    }

    /**
     * Updates the trajectory of the aircraft
     */

    /*private void updateTrajectory() {
        double currentAltitude = getAltitude();
        GeoPos currentPosition = getPosition();
        long currentTimestamp = getLastMessageTimeStampNs();

        if (trajectory.isEmpty() || (getPosition().equals(trajectory.get(trajectory.size() -1).pos())) ) {
            trajectory.add(new AirbornePos(currentPosition, currentAltitude));
            previousTimestamp = getLastMessageTimeStampNs();
        } else if (currentTimestamp == previousTimestamp) {
            trajectory.set(trajectory.size() - 1, new AirbornePos(currentPosition, currentAltitude));
        }
    }*/


    public AircraftData aircraftData() {
        return aircraftData;
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
        return trajectory;
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
        if (!Double.isNaN(getAltitude())){
            trajectory.add(new AirbornePos(position, getAltitude()));
        }
        previousTimestamp = getLastMessageTimeStampNs();
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
        if (trajectory.isEmpty()) {
            trajectory.add(new AirbornePos(getPosition(), altitude));
            previousTimestamp = getLastMessageTimeStampNs();
        }
        if (lastMessageTimeStampNs.get() == previousTimestamp) {
            trajectory.set(trajectory.size() - 1, new AirbornePos(getPosition(), altitude));
        }
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public IcaoAddress getIcaoAddress() {
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

    public ReadOnlyListProperty<AirbornePos> trajectoryProperty() {
        return new SimpleListProperty<>(unmodifiableTrajectory);
    }

}
