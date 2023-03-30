package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * @author @franklintra
 * @project Javions
 */
@SuppressWarnings("all") // TODO: 30/03/2023: remove this once done testing
public class AircraftState implements AircraftStateSetter {
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
    }

    @Override
    public void setCategory(int category) {
        this.category = category;
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
