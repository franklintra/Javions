package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
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


    public ObservableAircraftState(IcaoAddress icaoAddress, CallSign callSign, int category) {
        Objects.requireNonNull(icaoAddress);
        this.icaoAddress.setValue(icaoAddress);
        this.callSign.setValue(callSign);
        this.category.setValue(category);
        this.unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory.getValue());
    }

    public record AirbornePos(GeoPos pos, double altitude, long timeStampNs) {
        public AirbornePos {
            Preconditions.checkArgument(altitude >= 0);
            Preconditions.checkArgument(timeStampNs >= 0);
            Objects.requireNonNull(pos);
        }
    }


    private void updateTrajectory() {
        ObservableList<AirbornePos> currentTrajectory = trajectory.getValue();
        double currentAltitude = getAltitude();
        GeoPos currentPosition = getPosition();
        long currentTimestamp = getLastMessageTimeStampNs();

        if (currentTrajectory.isEmpty()) {
            currentTrajectory.add(new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
        } else {
            AirbornePos lastAirbornePos = currentTrajectory.get(currentTrajectory.size() - 1);
            GeoPos lastPosition = lastAirbornePos.pos();
            double lastAltitude = lastAirbornePos.altitude();
            long lastTimestamp = lastAirbornePos.timeStampNs();

            boolean positionChanged = !Objects.equals(currentPosition, lastPosition); //to avoid null pointer exception
            boolean altitudeChanged = currentAltitude != lastAltitude;
            boolean sameTimestamp = currentTimestamp == lastTimestamp;

            if (positionChanged || altitudeChanged) {
                if (sameTimestamp) {
                    currentTrajectory.set(currentTrajectory.size() - 1, new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
                } else {
                    currentTrajectory.add(new AirbornePos(currentPosition, currentAltitude, currentTimestamp));
                }
            }
        }
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

    /*
     * todo: check if this class is needed
     */
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

    @Override
    public void setPosition(GeoPos position) {
        this.position.setValue(position);
        updateTrajectory();
    }

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

    /**
     * todo: check if this is needed
     */
    public ReadOnlyProperty<ObservableList<AirbornePos>> trajectoryProperty() {
        return trajectory;
    }
}
