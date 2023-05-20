package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
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


public final class ObservableAircraftState implements AircraftStateSetter {
    // todo : comment this whole class !!!!! @chukla
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final Property<CallSign> callSign = new SimpleObjectProperty<>();
    private final DoubleProperty altitude = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocity = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty(Double.NaN);
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final Property<GeoPos> position = new SimpleObjectProperty<>();
    private final ObservableList<AirbornePos> observableTrajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(observableTrajectory);
    private final SimpleListProperty<AirbornePos> trajectory = new SimpleListProperty<>(unmodifiableTrajectory);

    private long previousTimestamp;

    /**
     * The constructor of the ObservableAircraftState class
     *
     * @param icaoAddress the ICAO address of the aircraft
     * @throws NullPointerException if the ICAO address is null
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        Objects.requireNonNull(icaoAddress);
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
    }

    public AircraftData aircraftData() {
        return aircraftData;
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
        if (!Double.isNaN(getAltitude())) {
            observableTrajectory.add(new AirbornePos(position, getAltitude()));
        }
        previousTimestamp = getLastMessageTimeStampNs();
    }

    /**
     * Returns the altitude of the aircraft
     *
     * @return the current altitude
     */
    public double getAltitude() {
        return altitude.get();
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
        if (position.getValue() == null) {
            return;
        }
        if (observableTrajectory.isEmpty()) {
            observableTrajectory.add(new AirbornePos(getPosition(), altitude));
            previousTimestamp = getLastMessageTimeStampNs();
        }
        if (lastMessageTimeStampNs.get() == previousTimestamp) {
            observableTrajectory.set(observableTrajectory.size() - 1, new AirbornePos(getPosition(), altitude));
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

    public ObservableList<AirbornePos> getObservableTrajectory() {
        return observableTrajectory;
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

    public ReadOnlyListProperty<AirbornePos> observableTrajectoryProperty() {
        return trajectory;
    }

    public record AirbornePos(GeoPos pos, double altitude) {
        /**
         * The constructor of the AirbornePos class
         *
         * @param pos      the position of the aircraft
         * @param altitude the altitude of the aircraft
         */
    }
}