package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author @franklintra
 * @project Javions
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {

    private static final int SUBTYPE_DATA_START_BIT = 21;
    private static final int SUBTYPE_START_BIT = 48;
    private static final int SUBTYPE_SIZE = 3;
    private static final List<Integer> SUBSONIC_SUBTYPES = Arrays.asList(1, 3);
    private static final List<Integer> SUPERSONIC_SUBTYPES = Arrays.asList(2, 4);
    private static final List<Integer> GROUND_SUBTYPES = Arrays.asList(1, 2);
    private static final List<Integer> AIR_SUBTYPES = Arrays.asList(3, 4);
    private static final List<Integer> VALID_SUBTYPES = Arrays.asList(1, 2, 3, 4);
    private static final double SUBSONIC_SPEED = Units.Speed.KNOT;
    private static final double SUPERSONIC_SPEED = Units.Speed.KNOT * 4;

    /**
     * Checks that the parameters are not null and that the time stamp is positive.
     *
     * @param timeStampNs    the time stamp in nanoseconds
     * @param icaoAddress    the ICAO description of the aircraft
     * @param speed          the speed of the aircraft
     * @param trackOrHeading the track or heading of the aircraft
     */
    public AirborneVelocityMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
        Objects.requireNonNull(icaoAddress);
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     *
     * @param rawMessage the raw message
     * @return the corresponding AirborneVelocityMessage
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        int subType = Bits.extractUInt(rawMessage.payload(), SUBTYPE_START_BIT, SUBTYPE_SIZE);
        if (GROUND_SUBTYPES.contains(subType)) {
            return groundVelocity(rawMessage, subType);
        }
        if (AIR_SUBTYPES.contains(subType)) {
            return airVelocity(rawMessage, subType);
        }
        return null;
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     * It decodes the speed and the angle of the aircraft for groundVelocity subType messages.
     *
     * @param m       the raw message
     * @param subType the subType of the given message
     * @return the corresponding AirborneVelocityMessage
     */
    private static AirborneVelocityMessage groundVelocity(RawMessage m, int subType) {
        double eastWest = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT + 11, 10) - 1;
        double northSouth = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT, 10) - 1;
        if (eastWest == -1 || northSouth == -1) {
            return null;
        }
        int horizontalDirection = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT + 21, 1) == 0 ? 1 : -1; // as on the unit circle : 1 to go up and -1 down
        int verticalDirection = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT + 10, 1) == 0 ? 1 : -1; // as on the unit circle : 1 to go up and -1 down
        eastWest *= horizontalDirection; // adjust the direction according to the bit that indicates it (21)
        northSouth *= verticalDirection; // adjust the direction according to the bit that indicates it (10)
        double theta = getAngle(eastWest, northSouth);
        double speed = Math.hypot(northSouth, eastWest);
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speedInMeterPerSecond(speed, subType), theta);
    }

    /**
     * Returns the AirborneVelocityMessage corresponding to the given raw message.
     * It decodes the speed and the angle of the aircraft for airVelocity subType messages.
     *
     * @param m       the raw message
     * @param subType the subType of the given message
     * @return the corresponding AirborneVelocityMessage
     */
    private static AirborneVelocityMessage airVelocity(RawMessage m, int subType) {
        int shBit = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT + 21, 1);
        double speed = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT, 10);
        if (shBit == 0 || speed == 0) {
            return null; // if shBit is 0, the message is invalid
        }

        // interpret the turn : Bits.extractUInt(m.payload(), 11, 10) as a unsigned integer
        long turnValue = Bits.extractUInt(m.payload(), SUBTYPE_DATA_START_BIT + 11, 10);
        double turn = Units.convertFrom(Math.scalb(turnValue, -10), Units.Angle.TURN); // divide by 1024 and convert to radian as specified in the standard
        speed--; // the speed is 1 less than the value
        return new AirborneVelocityMessage(m.timeStampNs(), m.icaoAddress(), speedInMeterPerSecond(speed, subType), turn);
    }

    /**
     * Returns the angle in radian between the vector (x, y) and the north direction (0 radian).
     * The angle is clockwise and positive from 0 to 2*PI.
     * This method is used for the ground velocity message.
     *
     * @param x the x value of the vector
     * @param y the y value of the vector
     * @return the calculated angle in radian
     */
    private static double getAngle(double x, double y) {
        // the valuers are passed as x and y instead of y and x because we want the complementary angle which is the angle between the vector and the north direction
        // this is why we use atan2(x, y) instead of atan2(y, x) because symmetrically, atan2(y, x) would give the angle between the vector and the east direction
        double angle = Math.atan2(x, y);
        if (angle < 0) {
            angle += Math.PI * 2; // if the angle is negative, we add 2*PI to get a positive angle
        }
        return angle;
    }

    /**
     * Returns the speed in meter per second according to the subtype (units are different for each subtype).
     *
     * @param speed   the speed in the unit specified by the subtype
     * @param subType the subtype of the message
     * @return the speed in meter per second (m/s)
     */
    private static double speedInMeterPerSecond(double speed, int subType) {
        Preconditions.checkArgument(VALID_SUBTYPES.contains(subType));
        if (SUBSONIC_SUBTYPES.contains(subType)) {
            return Units.convertFrom(speed, SUBSONIC_SPEED);
        }
        if (SUPERSONIC_SUBTYPES.contains(subType)) {
            return Units.convertFrom(speed, SUPERSONIC_SPEED);
        }
        throw new IllegalArgumentException("The subtype is not valid");
    }
}