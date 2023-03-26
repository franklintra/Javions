package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * @author @franklintra
 * @project Javions
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message {
    /**
     * Checks that the parameters are not null and that the time stamp is positive.
     * @param timeStampNs the time stamp in nanoseconds
     * @param icaoAddress the ICAO description of the aircraft
     * @param speed the speed of the aircraft
     * @param trackOrHeading the track or heading of the aircraft
     */
    public AirborneVelocityMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     * @param rawMessage the raw message
     * @return the corresponding AirborneVelocityMessage
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        int subType = Bits.extractUInt(rawMessage.payload(), 48, 3);
        return switch (subType) {
            case 1, 2 -> groundVelocity(rawMessage, subType);
            case 3, 4 -> airVelocity(rawMessage, subType);
            default -> null;
        };
    }

    private static AirborneVelocityMessage groundVelocity(RawMessage m, int subType) {
        int horizontalDirection = (Bits.extractUInt(m.payload(), 21, 1) == 0 ? -1 : 1);
        int verticalDirection = (Bits.extractUInt(m.payload(), 10, 1) == 0 ? -1 : 1);
        double eastWest = Bits.extractUInt(m.payload(), 11, 10) - 1;
        double northSouth = Bits.extractUInt(m.payload(), 0, 10) - 1;
        if (eastWest == -1 || northSouth == -1) {
            return null;
        }
        eastWest *= horizontalDirection;
        northSouth *= verticalDirection;
        double theta = Math.atan2(northSouth, eastWest);
        double speed = Math.hypot(northSouth, eastWest);
        speed = Units.convertFrom(speed, subType == 1 ? Units.Speed.KNOT : 4*Units.Speed.KNOT);
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speed, theta);
    }

    private static AirborneVelocityMessage airVelocity(RawMessage m, int subType) {
        int SH = Bits.extractUInt(m.payload(), 21, 1);
        if (SH == 0) {
            return null;
        }
        // interpret the turn : Bits.extractUInt(m.payload(), 11, 10) as a unsigned integer
        long unsignedInt = Integer.parseUnsignedInt("0b" + Integer.toBinaryString(Bits.extractUInt(m.payload(), 11, 10)));
        double turn = Math.scalb(unsignedInt, -10);
        turn = Units.convertFrom(turn, Units.Angle.TURN);
        double speed = Units.convertFrom(Bits.extractUInt(m.payload(), 0, 10), (subType == 3 ? Units.Speed.KNOT : 4*Units.Speed.KNOT));
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speed, turn);
    }
}