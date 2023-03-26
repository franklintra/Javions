package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;

import java.util.Arrays;

/**
 * @author @franklintra
 * @project Javions
 */
public final class MessageParser {
    private MessageParser() {}

    public static Message parse(RawMessage message) {
        int typeCode = Bits.extractUInt(message.payload(), 51, 5);
        if (Arrays.asList(1, 2, 3, 4).contains(typeCode)) {
            return AircraftIdentificationMessage.of(message);
        } else if ((9 <= typeCode && typeCode <= 18) || (20 <= typeCode && typeCode <= 22)) {
            return AirbornePositionMessage.of(message);
        } else if (typeCode == 19) {
            return AirborneVelocityMessage.of(message);
        }
        return null;
    }
}
