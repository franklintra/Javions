package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public interface AircraftStateSetter {
    void setLastMessageTimeStampNs(long timeStampNs); // Changes the time stamp of the last message received from the aircraft.

    void setCategory(int category); // Changes the category of the aircraft.

    void setCallSign(CallSign callSign); // Changes the call sign of the aircraft.

    void setPosition(GeoPos position); // Changes the position of the aircraft to the given position.

    void setAltitude(double altitude); // Changes the altitude of the aircraft to the given altitude.

    void setVelocity(double velocity); // Changes the velocity of the aircraft to the given velocity.

    void setTrackOrHeading(double trackOrHeading); // Changes the direction of the aircraft to the given direction.
}
