package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * @author @franklintra
 * @project Javions
 */
public class AircraftState implements AircraftStateSetter {
    //todo : remove this class once done testing
    private long timeStampNs;
    private int category;
    private CallSign callSign;
    private GeoPos position;
    private double altitude;
    private double velocity;
    private double trackOrHeading;
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.timeStampNs = timeStampNs;
        System.out.println("LastMessageTimeStampNs: " + timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        this.category = category;
        System.out.println("Category: " + category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign = callSign;
        System.out.println("CallSign: " + callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position = position;
        System.out.println("Position: " + position);
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude = altitude;
        System.out.println("Altitude: " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity = velocity;
        System.out.println("Velocity: " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading = trackOrHeading;
        System.out.println("TrackOrHeading: " + trackOrHeading);
    }
}
