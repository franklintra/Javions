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
        // the subType is an unsigned integer of 3 bits
        int subType = (int) Integer.toUnsignedLong(Bits.extractUInt(rawMessage.payload(), 48, 3));
        return switch (subType) {
            case 1, 2 -> groundVelocity(rawMessage, subType);
            case 3, 4 -> airVelocity(rawMessage, subType);
            default -> null;
        };
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     * It decodes the speed and the angle of the aircraft for groundVelocity subType messages.
     * @param m the raw message
     * @param subType the subType of the given message
     * @return the corresponding AirborneVelocityMessage
     */
    private static AirborneVelocityMessage groundVelocity(RawMessage m, int subType) {
        int horizontalDirection = Bits.extractUInt(m.payload(), 21, 1) == 0 ? -1 : 1;
        int verticalDirection = Bits.extractUInt(m.payload(), 10, 1) == 0 ? -1 : 1;
        double eastWest = Bits.extractUInt(m.payload(), 11, 10) - 1;
        double northSouth = Bits.extractUInt(m.payload(), 0, 10) - 1;
        if (eastWest == -1 || northSouth == -1) {
            return null;
        }
        eastWest *= horizontalDirection; // adjust the direction according to the bit that indicates it (21)
        northSouth *= verticalDirection; // adjust the direction according to the bit that indicates it (10)
        double theta = getAngle(eastWest, northSouth);
        double speed = Math.hypot(northSouth, eastWest);
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speedInMeterPerSecond(speed, subType), theta);
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     * It decodes the speed and the angle of the aircraft for airVelocity subType messages.
     * @param m the raw message
     * @param subType the subType of the given message
     * @return the corresponding AirborneVelocityMessage
     */
    private static AirborneVelocityMessage airVelocity(RawMessage m, int subType) {
        int SH = Bits.extractUInt(m.payload(), 21, 1);
        if (SH == 0) return null; // if SH is 0, the message is invalid
        // interpret the turn : Bits.extractUInt(m.payload(), 11, 10) as a unsigned integer
        long turnValue = Integer.toUnsignedLong(Bits.extractUInt(m.payload(), 11, 10));
        double turn = Units.convertFrom(Math.scalb(turnValue, -10), Units.Angle.TURN); // divide by 1024 and convert to radian as specified in the standard
        double speed = Bits.extractUInt(m.payload(), 0, 10);
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speedInMeterPerSecond(speed, subType), turn);
    }

    /**
     * Returns the angle in radian between the vector (x, y) and the north direction (0 radian).
     * The angle is clockwise and positive from 0 to 2*PI.
     * This method is used for the ground velocity message.
     * @param x the x value of the vector
     * @param y the y value of the vector
     * @return the calculated angle in radian
     */
    private static double getAngle(double x, double y) {
        double angle = - (Math.atan2(y, x) - (Math.PI / 2)); // align angle with north and make it clockwise
        return angle < 0 ? angle + 2*Math.PI : angle; // make sure angle is positive
    }

    /**
     * Returns the speed in meter per second according to the subtype (units are different for each subtype).
     * @param speed the speed in the unit specified by the subtype
     * @param subType the subtype of the message
     * @return the speed in meter per second (m/s)
     */
    private static double speedInMeterPerSecond(double speed, int subType) {
        return switch (subType) {
            case 1, 3 -> Units.convertFrom(speed, Units.Speed.KNOT);
            case 2, 4 -> Units.convertFrom(speed, 4*Units.Speed.KNOT);
            default -> throw new IllegalArgumentException("Subtype must be 1 or 2");
        };
    }
}