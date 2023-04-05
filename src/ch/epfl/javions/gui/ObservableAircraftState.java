package ch.epfl.javions.gui;/*
/**
 * @author @chukla
 * @project Javions
 */

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;


public final class ObservableAircraftState implements AircraftStateSetter {
    private final String icaoAddress;
    private final int category;
    private final String callSign;
    private final double altitude;
    private final double velocity;
    private final double trackOrHeading;
    private final GeoPos position;
    private final AirbornePositionMessage lastMessageTimeStampNs;

    public ObservableAircraftState(String icaoAddress, int category, String callSign, double altitude, double velocity, double trackOrHeading, GeoPos position, AirbornePositionMessage lastMessageTimeStampNs) {
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

    public String getCallSign() {
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

    public AirbornePositionMessage getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs;
    }


    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {

    }

    @Override
    public void setCategory(int category) {

    }

    @Override
    public void setCallSign(CallSign callSign) {

    }

    @Override
    public void setPosition(GeoPos position) {

    }

    @Override
    public void setAltitude(double altitude) {

    }

    @Override
    public void setVelocity(double velocity) {

    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {

    }
}
