package ch.epfl.javions.adsb;/*

/**
 * @author @chukla
 * @project Javions
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private final T stateSetter;
    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;
    private final AircraftState state = new AircraftState();

    /**
     * Constructs a new AirCraftStateAccumulator object with the given state setter.
     *
     * @param stateSetter the state setter
     * @throws NullPointerException if the state setter is null
     */
    public AircraftStateAccumulator(T stateSetter) {
        if (stateSetter == null) {
            throw new NullPointerException("stateSetter cannot be null");
        }
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
        state.setLastMessageTimeStampNs(message.timeStampNs());


        switch (message) {
            case AircraftIdentificationMessage aim -> {
                state.setCategory(aim.category());
                state.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                if (apm.parity() == 0) {
                    lastEvenMessage = apm;
                } else {
                    lastOddMessage = apm;
                }
                state.setAltitude(apm.altitude());
                if (lastEvenMessage != null && lastOddMessage != null) {
                    long diff = apm.timeStampNs() - (apm.parity() == 0 ? lastOddMessage.timeStampNs() : lastEvenMessage.timeStampNs());
                    if (diff <= 10e9) { // 10 seconds in nanoseconds
                        state.setPosition(CprDecoder.decodePosition(apm.x(), apm.y(), lastEvenMessage.x(), lastEvenMessage.y(), apm.parity()));
                    }
                }



            }
            case AirborneVelocityMessage avm -> {
                state.setVelocity(avm.speed()); // TODO: 3/27/2023 check
                state.setTrackOrHeading(avm.trackOrHeading()); // TODO: 3/27/2023 check
            }
            default -> System.out.println("Other type of Message");
        }

    }

    /**
     * Returns the last message with the opposite parity.
     *
     * @param message the message to check
     * @return the last message with the opposite parity
     */

    private long lastOppositeTimeStamp(AirbornePositionMessage message) {
        if (message.parity() == 0) {
            return lastOddMessage != null ? lastOddMessage.timeStampNs() : 0L;
        } else {
            return lastEvenMessage != null ? lastEvenMessage.timeStampNs() : 0L;
        }
    }


}
