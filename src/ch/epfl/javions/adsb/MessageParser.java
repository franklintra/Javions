package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;

/**
 * @author @franklintra
 * @project Javions
 */
public final class MessageParser {
    /**
     * This class is not instantiable hence the private constructor.
     */
    private MessageParser() {}

    /**
     * Parses the given raw message into a message object if the message is of a known type.
     * @param message the raw message to parse
     * @return the parsed message or null if the message is of an unknown type
     */
    public static Message parse(RawMessage message) {
        int typeCode = Bits.extractUInt(message.payload(), 51, 5);
        if (1 <= typeCode && typeCode <= 4) {
            return AircraftIdentificationMessage.of(message);
        } else if ((9 <= typeCode && typeCode <= 18) || (20 <= typeCode && typeCode <= 22)) {
            return AirbornePositionMessage.of(message);
        } else if (typeCode == 19) {
            return AirborneVelocityMessage.of(message);
        }
        return null;
    }
}
