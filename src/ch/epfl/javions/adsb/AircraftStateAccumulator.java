package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * @author @chukla (357550)
 * @author @franklintra (362694)
 * @project Javions
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static long TEN_SECONDS_IN_NS = (long) 10e9;
    private final T stateSetter;
    // This is a buffer of size two where the even messages are stored at index 0 and the odd messages are stored at index 1.
    private final AirbornePositionMessage[] lastMessages = new AirbornePositionMessage[2];

    /**
     * Constructs a new AirCraftStateAccumulator object with the given state setter.
     *
     * @param stateSetter the state setter
     * @throws NullPointerException if the state setter is null
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    /**
     * Returns the state of the aircraft.
     *
     * @return the state of the aircraft
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * Updates the state of the aircraft with the given message.
     *
     * @param message the message to update the state with
     */
    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());

        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                lastMessages[apm.parity()] = apm;
                stateSetter.setAltitude(apm.altitude());
                if (lastMessages[0] != null && lastMessages[1] != null) {
                    long diff = apm.timeStampNs() - lastMessages[1 - apm.parity()].timeStampNs();
                    if (diff <= TEN_SECONDS_IN_NS) { // 10 seconds in nanoseconds
                        GeoPos pos = CprDecoder.decodePosition(lastMessages[0].x(), lastMessages[0].y(), lastMessages[1].x(), lastMessages[1].y(), apm.parity());
                        if (pos != null) {
                            stateSetter.setPosition(pos);
                        }
                    }
                }
            }
            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> {
            }
        }
    }
}
