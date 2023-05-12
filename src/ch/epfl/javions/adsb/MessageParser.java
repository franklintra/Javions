package ch.epfl.javions.adsb;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public final class MessageParser {
    /**
     * This class is not instantiable hence the private constructor.
     */
    private MessageParser() {
    }

    /**
     * Parses the given raw message into a message object if the message is of a known type.
     *
     * @param message the raw message to parse
     * @return the parsed message or null if the message is of an unknown type
     */
    public static Message parse(RawMessage message) {
        int typeCode = message.typeCode();
        switch (typeCode) {
            /*
             * Aircraft identification message
             */
            case 1, 2, 3, 4 -> {
                return AircraftIdentificationMessage.of(message);
            }
            /*
             * Airborne position message
             */
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 -> {
                return AirbornePositionMessage.of(message);
            }
            /*
             * Airborne velocity message
             */
            case 19 -> {
                return AirborneVelocityMessage.of(message);
            }
            default -> {
                return null;
            }
        }
    }
}
