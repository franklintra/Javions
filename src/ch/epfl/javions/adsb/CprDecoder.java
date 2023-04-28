package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public final class CprDecoder {
    //these values never change so they are declared as class constants to be used in the static methods
    // the number of latitude zones for even and odd messages
    private static final int[] NUMBER_OF_LATITUDE_ZONES = {60, 59};
    // the width of each of these latitude zones for even and odd messages
    private static final double[] WIDTH_OF_LATITUDE_ZONES = {1d / NUMBER_OF_LATITUDE_ZONES[0], 1d / NUMBER_OF_LATITUDE_ZONES[1]};

    /**
     * This class is not meant to be instantiated. Hence, the constructor is private.
     */
    private CprDecoder() {
    }

    /**
     * Throughout this class, the following notation is used:
     * (mostRecent==0)?x0:x1 allows to choose between x0 and x1 depending on the value of mostRecent without using an if statement or a List
     *
     * @param x0         - longitude of an even message aircraft position
     * @param y0         - latitude of an even message aircraft position
     * @param x1         - longitude of an odd message aircraft position
     * @param y1         - latitude of an odd message aircraft position
     * @param mostRecent - 0 if the most recent message is even, 1 if the most recent message is odd
     * @return the decoded aircraft position
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);
        //need to calculate both latitudes because they are used to calculate nLong and
        //check that the plane doesn't cross a longitude zone (rare case but still)
        double currentMessageLatitude = normalizeT32Angle(latitudeCalculator(y0, y1, mostRecent));
        double otherMessageLatitude = normalizeT32Angle(latitudeCalculator(y0, y1, 1 - mostRecent));

        int currentMessageNumberOfLongitudes = numberOfLongitudeZones(currentMessageLatitude);
        int otherMessageNumberOfLongitudes = numberOfLongitudeZones(otherMessageLatitude);

        if (!GeoPos.isValidLatitudeT32((int) Math.rint(Units.convert(currentMessageLatitude, Units.Angle.TURN, Units.Angle.T32)))
                || currentMessageNumberOfLongitudes != otherMessageNumberOfLongitudes) {
            return null;
        }

        //if the longitude is greater than 180°, then it should be 180° - the longitude so one turn is subtracted
        double currentMessageLong = normalizeT32Angle(longitudeCalculator(x0, x1, currentMessageNumberOfLongitudes, mostRecent));

        return new GeoPos(
                (int) Math.rint(Units.convert(currentMessageLong, Units.Angle.TURN, Units.Angle.T32)),
                (int) Math.rint(Units.convert(currentMessageLatitude, Units.Angle.TURN, Units.Angle.T32))
        );
    }


    /**
     * Calculates the latitude in turn from the given y0 and y1 values and the given index.
     * Both y0 and y1 are needed no matter which index is given
     *
     * @param y0        : local latitude in turn of the even message
     * @param y1        : local latitude in turn of the odd message
     * @param calcIndex : 0 if the latitude is calculated from y0, 1 if the latitude is calculated from y1
     * @return the latitude in turn
     */
    private static double latitudeCalculator(double y0, double y1, int calcIndex) {
        // this is a temporary value used to compute latZone0 and latZone1
        int lat = (int) Math.rint(y0 * NUMBER_OF_LATITUDE_ZONES[1] - y1 * NUMBER_OF_LATITUDE_ZONES[0]);
        double currentMessageY = (calcIndex == 0) ? y0 : y1; // choose between y0 and y1 depending on calcIndex
        return WIDTH_OF_LATITUDE_ZONES[calcIndex] * ((lat < 0 ? lat + NUMBER_OF_LATITUDE_ZONES[calcIndex] : lat) + currentMessageY);
    }

    /**
     * Calculates the longitude in turn from the given x0 and x1 values and the given index.
     *
     * @param numberOfEvenLongitudeZones : the number of longitude zones for even messages
     * @param x0                         : local longitude in turn of the even message
     * @param x1                         : local longitude in turn of the odd message
     * @param calcIndex                  : 0 if the longitude is calculated from x0, 1 if the longitude is calculated from x1
     * @return the longitude in turn
     */
    private static double longitudeCalculator(double x0, double x1, int numberOfEvenLongitudeZones, int calcIndex) {
        int[] numberOfLongitudeZones = {numberOfEvenLongitudeZones, numberOfEvenLongitudeZones - 1};
        double widthOfLongitudeZones = 1d / numberOfLongitudeZones[calcIndex];
        int longitude = (int) Math.rint(x0 * numberOfLongitudeZones[1] - x1 * numberOfLongitudeZones[0]);
        longitude = (longitude < 0 ? longitude + numberOfLongitudeZones[calcIndex] : longitude);
        double currentMessageX = calcIndex == 0 ? x0 : x1;
        // if numberOfEvenLongitudeZones is 1, then the longitude is completely determined by x0 or x1
        return (numberOfLongitudeZones[0] == 1) ? currentMessageX : (widthOfLongitudeZones * (longitude + currentMessageX));
    }


    /**
     * Calculates the number of longitude Zones from a given latitude.
     *
     * @param lat : latitude in turn
     * @return the number of longitude zones
     */
    private static int numberOfLongitudeZones(double lat) {
        //the following formulas are both taken from the ADS-B specification
        double cos = Math.cos(Units.convertFrom(lat, Units.Angle.TURN));
        double A = Math.acos(1 -
                (1 - Math.cos(Units.Angle.TURN * WIDTH_OF_LATITUDE_ZONES[0]))
                        / (cos*cos)
        );
        return (Double.isNaN(A) ? 1 : (int) Math.floor((Math.PI * 2) / A));
    }

    /**
     * If the given value is greater than 0.5, then it is subtracted by 1.
     * This is used to normalize the latitude and longitude values to ensure that they are between -0.5 and 0.5.
     *
     * @param angle : the value to normalize
     * @return the normalized value
     */
    private static double normalizeT32Angle(double angle) {
        return angle > 0.5 ? angle - 1 : angle;
    }
}