package ch.epfl.javions.adsb;

import java.util.Set;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public final class MessageParser {

    private final static Set<Integer> IDENTIFICATION_MESSAGES_TYPECODES = Set.of(1, 2, 3, 4);
    private final static Set<Integer> AIRBORNE_POSITION_MESSAGE_TYPECODES = Set.of(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22);
    private final static Set<Integer> AIRBORNE_VELOCITY_MESSAGE_TYPECODES = Set.of(19);

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
        if (IDENTIFICATION_MESSAGES_TYPECODES.contains(typeCode)) {
            return AircraftIdentificationMessage.of(message);
        } else if (AIRBORNE_POSITION_MESSAGE_TYPECODES.contains(typeCode)) {
            return AirbornePositionMessage.of(message);
        } else if (AIRBORNE_VELOCITY_MESSAGE_TYPECODES.contains(typeCode)) {
            return AirborneVelocityMessage.of(message);
        }
        return null;
    }

    //todo : remove that method (just to prove a point)
    public static Message parseAsTheyWant(RawMessage message) {
        int typeCode = message.typeCode();
        switch (typeCode) {
            case 1, 2, 3, 4 -> {
                return AircraftIdentificationMessage.of(message);
            }
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 -> {
                return AirbornePositionMessage.of(message);
            }
            case 19 -> {
                return AirborneVelocityMessage.of(message);
            }
            default -> {
                return null;
            }
        }
    }
}
