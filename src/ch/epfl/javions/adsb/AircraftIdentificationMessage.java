package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * @author @franklintra
 * @project Javions
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) {
    /**
     * Checks that the parameters are not null and that the time stamp is positive.
     * @param timeStampNs the time stamp in nanoseconds
     * @param icaoAddress the ICAO address of the aircraft
     * @param category the category of the aircraft
     * @param callSign the call sign of the aircraft
     */
    public AircraftIdentificationMessage {
        Preconditions.checkArgument(timeStampNs >= 0);
        if (callSign == null || icaoAddress == null) {
            throw new NullPointerException("One of the parameters is null");
        }
    }

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        //fixme : check how extractUInt works exactly (is start from the left or the right? ) and does it decode right to left or left to right?
        Preconditions.checkArgument(rawMessage.downLinkFormat() == 17);
        int category = Bits.extractUInt(rawMessage.payload(), 48, 3);
        StringBuilder callSignString = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            callSignString.insert(0, (char) (Bits.extractUInt(rawMessage.payload(), i * 6, 6))+18);
        }
        CallSign callSign = new CallSign("A");
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }
}
