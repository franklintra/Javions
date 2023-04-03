package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * @author @franklintra
 * @project Javions
 */
public final class CprDecoder {
    //these values never change so they are declared as class constants to be used in the static methods
    // the number of latitude zones for even and odd messages
    private static final int[] N_LATS = {60, 59};
    // the width of each of these latitude zones
    private static final double[] WIDTH_LATS = {1d / N_LATS[0], 1d / N_LATS[1]};

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
        //need to calculate both latitudes because they are used to calculate nLong and check that the plane doesn't cross a longitude zone (rare case but still)
        double currentMessageLat = normalizeT32Angle(latitudeCalculator(y0, y1, mostRecent));
        double otherMessageLat = normalizeT32Angle(latitudeCalculator(y0, y1, 1 - mostRecent));

        if (numberOfLongitudeZones(currentMessageLat) != numberOfLongitudeZones(otherMessageLat)) {
            return null; // if the two values are not equal, then the plane has crossed a longitude zone
        }
        int nLong = numberOfLongitudeZones(mostRecent == 0 ? currentMessageLat : otherMessageLat); // the number of longitude zones for even messages (odd value is even - 1)
        double currentMessageLong = normalizeT32Angle(longitudeCalculator(x0, x1, nLong, mostRecent)); //if the longitude is greater than 180°, then it should be 180° - the longitude so one turn is subtracted

        return isValidData(currentMessageLong, currentMessageLat);
    }

    /**
     * Checks if the given latitude and longitude are valid and returns a GeoPos object if they are with rounded values of T32 longitude and latitude.
     *
     * @param longitude : longitude in turn
     * @param latitude  : latitude in turn
     * @return a GeoPos object with rounded values of T32 longitude and latitude if the given latitude and longitude are valid, null otherwise
     */
    private static GeoPos isValidData(double longitude, double latitude) {
        if (GeoPos.isValidLatitudeT32((int) Math.rint(Units.convert(latitude, Units.Angle.TURN, Units.Angle.T32)))) {
            return new GeoPos(
                    (int) Math.rint(Units.convert(longitude, Units.Angle.TURN, Units.Angle.T32)),
                    (int) Math.rint(Units.convert(latitude, Units.Angle.TURN, Units.Angle.T32))
            );
        }
        return null;
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
        int lat = (int) Math.rint(y0 * N_LATS[1] - y1 * N_LATS[0]); // this is a temporary value used to compute latZone0 and latZone1
        double actualY = calcIndex == 0 ? y0 : y1; // choose between y0 and y1 depending on calcIndex
        return WIDTH_LATS[calcIndex] * ((lat < 0 ? lat + N_LATS[calcIndex] : lat) + actualY);
    }

    /**
     * Calculates the longitude in turn from the given x0 and x1 values and the given index.
     *
     * @param nLong     : the number of longitude zones for even messages
     * @param x0        : local longitude in turn of the even message
     * @param x1        : local longitude in turn of the odd message
     * @param calcIndex : 0 if the longitude is calculated from x0, 1 if the longitude is calculated from x1
     * @return the longitude in turn
     */
    private static double longitudeCalculator(double x0, double x1, int nLong, int calcIndex) {
        int[] nLongValues = {nLong, nLong - 1};
        double widthLong = 1d / nLongValues[calcIndex];
        int longitude = (int) Math.rint(x0 * nLongValues[1] - x1 * nLongValues[0]);
        longitude = (longitude < 0 ? longitude + nLongValues[calcIndex] : longitude);
        double X = calcIndex == 0 ? x0 : x1;
        return (nLongValues[0] == 1) ? X : (widthLong * (longitude + X)); // if nLong is 1, then the longitude is completely determined by x0 or x1
    }


    /**
     * Calculates the number of longitude Zones from a given latitude.
     *
     * @param lat : latitude in turn
     * @return the number of longitude zones
     */
    private static int numberOfLongitudeZones(double lat) {
        //the following formulas are both taken from the ADS-B specification
        double A = Math.acos(1 - (1 - Math.cos(Math.PI * 2 * WIDTH_LATS[0])) / Math.pow(Math.cos(Units.convert(lat, Units.Angle.TURN, Units.Angle.RADIAN)), 2));
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