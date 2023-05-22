package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;


/**
 * @author @franklintra (362694)
 * @project Javions
 * <p>
 * An AircraftStateSetter is an object that can change the state of an aircraft.
 */
public interface AircraftStateSetter {
    /**
     * Changes the time stamp of the last message received from the aircraft.
     *
     * @param timeStampNs the new time stamp in nanoseconds
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Changes the category of the aircraft.
     * This method is used to change the category of the aircraft when the category is unknown.
     *
     * @param category the new category
     */
    void setCategory(int category); // Changes the category of the aircraft.

    /**
     * Changes the call sign of the aircraft.
     * This method is used to change the call sign of the aircraft when the call sign is unknown.
     *
     * @param callSign the new call sign
     */
    void setCallSign(CallSign callSign); // Changes the call sign of the aircraft.

    /**
     * Changes the position of the aircraft.
     *
     * @param position the new position
     */
    void setPosition(GeoPos position); // Changes the position of the aircraft to the given position.

    /**
     * Changes the altitude of the aircraft.
     *
     * @param altitude the new altitude
     */
    void setAltitude(double altitude); // Changes the altitude of the aircraft to the given altitude.

    /**
     * Changes the velocity of the aircraft.
     *
     * @param velocity the new velocity
     */
    void setVelocity(double velocity); // Changes the velocity of the aircraft to the given velocity.

    /**
     * Changes the direction of the aircraft.
     *
     * @param trackOrHeading the new direction
     */
    void setTrackOrHeading(double trackOrHeading); // Changes the direction of the aircraft to the given direction.
}
