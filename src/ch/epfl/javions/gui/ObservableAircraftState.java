package ch.epfl.javions.gui;/*
/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;


public final class ObservableAircraftState implements AircraftStateSetter {
    private final String icaoAddress;
    private int category;
    private CallSign callSign;
    private double altitude;
    private double velocity;
    private double trackOrHeading;
    private GeoPos position;
    private long lastMessageTimeStampNs;

    public ObservableAircraftState(String icaoAddress, int category, CallSign callSign, double altitude, double velocity, double trackOrHeading, GeoPos position, long lastMessageTimeStampNs) {
        this.icaoAddress = icaoAddress;
        this.category = category;
        this.callSign = callSign;
        this.altitude = altitude;
        this.velocity = velocity;
        this.trackOrHeading = trackOrHeading;
        this.position = position;
        this.lastMessageTimeStampNs = lastMessageTimeStampNs;
    }

    public String getIcaoAddress() {
        return icaoAddress;
    }

    public double getCategory() {
        return category;
    }

    public CallSign getCallSign() {
        return callSign;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getTrackOrHeading() {
        return trackOrHeading;
    }

    public GeoPos getPosition() {
        return position;
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs;
    }


    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.lastMessageTimeStampNs = timeStampNs;
    }

    @Override
    public void setCategory(int category) {
        this.category = category;
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign = callSign;
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position = position;
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading = trackOrHeading;
    }
}
