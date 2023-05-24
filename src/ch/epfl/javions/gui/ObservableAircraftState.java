package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftRegistration;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

/**
 * The ObservableAircraftState class represents the observable state of an aircraft. It provides properties for various
 * attributes such as ICAO address, aircraft data, last message timestamp, category, call sign, position, altitude,
 * velocity, and track or heading. It also maintains an observable trajectory list that tracks the aircraft's position
 * over time.
 *
 * This class implements the AircraftStateSetter interface to set the state attributes.
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final Property<CallSign> callSign = new SimpleObjectProperty<>();
    private final Property<GeoPos> position = new SimpleObjectProperty<>();
    private final DoubleProperty altitude = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocity = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty(Double.NaN);
    private final ObservableList<AirbornePos> trajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> observableUnmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    private long previousTimestamp;

    /**
     * Constructs an ObservableAircraftState object with the given ICAO address and aircraft data.
     *
     * @param icaoAddress  the ICAO address of the aircraft
     * @param aircraftData the aircraft data
     * @throws NullPointerException if the ICAO address is null
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
    }

    public AircraftData aircraftData() {
        return aircraftData;
    }

    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    public int getCategory() {
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public CallSign getCallSign() {
        return callSign.getValue();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.setValue(callSign);
    }

    public GeoPos getPosition() {
        return position.getValue();
    }

    /**
     * Sets the position of the aircraft
     *
     * @param position the position of the aircraft
     * @throws NullPointerException if the position is null
     */
    @Override
    public void setPosition(GeoPos position) {
        Objects.requireNonNull(position);
        this.position.setValue(position);
        //If the position is different from the last element in the trajectory, the current position and altitude are added as a new element to the trajectory.
        if (!Double.isNaN(getAltitude())) {
            trajectory.add(new AirbornePos(position, getAltitude()));
        }
        previousTimestamp = getLastMessageTimeStampNs();
    }

    public double getAltitude() {
        return altitude.get();
    }

    /**
     * Sets the altitude of the aircraft.
     *
     * @param altitude the altitude of the aircraft
     */
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if (position.getValue() == null) {
            return;
        }
        // If the trajectory is empty, the current position and altitude are added as a new element to the trajectory.
        if (trajectory.isEmpty()) {
            trajectory.add(new AirbornePos(getPosition(), altitude));
            previousTimestamp = getLastMessageTimeStampNs();
        }
        // If the timestamps of the current and previous messages match, the last trajectory element is updated with the current position and altitude.
        if (lastMessageTimeStampNs.get() == previousTimestamp) {
            trajectory.set(trajectory.size() - 1, new AirbornePos(getPosition(), altitude));
        }
    }

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public ReadOnlyProperty<CallSign> callSignProperty() {
        return callSign;
    }
    public ReadOnlyProperty<GeoPos> positionProperty() {
        return position;
    }

    public ObservableList<AirbornePos> getTrajectory() {
        return observableUnmodifiableTrajectory;
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

    /**
     * The AirbornePos record class
     * Represents the position of the aircraft in the air
     * (GeoPos + altitude)
     *
     * @param pos      the position of the aircraft
     * @param altitude the altitude of the aircraft
     */
    public record AirbornePos(GeoPos pos, double altitude) {
    }
}