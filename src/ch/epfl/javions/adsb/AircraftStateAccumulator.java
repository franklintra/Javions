package ch.epfl.javions.adsb;/*

/**
 * @author @chukla
 * @project Javions
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {

    private final T stateSetter;
    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;
    private AircraftState state = new AircraftState();

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
                state.setAltitude(apm.altitude());
                if (isPositionDetermined(apm)) {
                    state.setPosition(CprDecoder.decodePosition(apm.x(), apm.y(), lastEvenMessage.x(), lastEvenMessage.y(), apm.parity())); // TODO: 3/28/2023 how to get position
                }
                if (apm.parity() == 0) {
                    lastEvenMessage = apm;
                } else {
                    lastOddMessage = apm;
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
     * Returns the time stamp of the last message of the opposite parity.
     *
     * @param message the message to check
     * @return the time stamp of the last message of the opposite parity
     */
    private long lastOppositeTimeStamp(AirbornePositionMessage message) {
        return message.parity() == 0 ? lastOddMessage.timeStampNs() : lastEvenMessage.timeStampNs();
    }

    /**
     * Returns true if the position of the aircraft can be determined.
     *
     * @param message the message to check
     * @return true if the position of the aircraft can be determined
     */
    private boolean isPositionDetermined(AirbornePositionMessage message) {
        if (lastOddMessage == null && lastEvenMessage == null) {
            return false; // TODO: 3/28/2023 check if this doesnt cause any issues in their tests
        }
        long diff = message.timeStampNs() - lastOppositeTimeStamp(message);
        return diff <= 10_000_000_000L; // 10 seconds in nanoseconds
    }


}
