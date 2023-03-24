package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * @author @franklintra
 * @project Javions
 */
public class CprDecoder {
    /**
     * This class is not meant to be instantiated. Hence, the constructor is private.
     */
    private CprDecoder() {
    }

    /**
     * @param x0 - longitude of an even message aircraft position
     * @param y0 - latitude of an even message aircraft position
     * @param x1 - longitude of an odd message aircraft position
     * @param y1 - latitude of an odd message aircraft position
     * @param mostRecent - 0 if the most recent message is even, 1 if the most recent message is odd
     * @return the decoded aircraft position
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);
        int[] nLat = {60, 59}; // the number of latitude zones for even and odd messages
        int[] nLong = new int[2]; // the number of longitude zones for even and odd messages
        double[] widthLat = {1d/nLat[0], 1d/nLat[1]}; // necessary for both latitude and longitude
        //need to calculate both evenLat and oddLat because they are used to calculate nLong and check that the plane doesn't cross a longitude zone (rare case but still)
        double evenLat;
        double oddLat;
        double actualLong;

        { // calculate latitude
            int lat = (int) Math.rint(y0 * nLat[1] - y1 * nLat[0]); // this is a temporary value used to compute latZone0 and latZone1
            int latZone = lat < 0 ? lat + nLat[mostRecent] : lat;
            evenLat = widthLat[0] * (latZone + y0);
            oddLat = widthLat[1] * (latZone + y1);
        }

        { // calculate longitude
            final double numerator = 1 - Math.cos(Math.PI * 2 * widthLat[0]);
            double A0 = Math.acos(1 - numerator / Math.pow(Math.cos(Units.convert(evenLat, Units.Angle.TURN, Units.Angle.RADIAN)), 2));
            double A1 = Math.acos(1 - numerator / Math.pow(Math.cos(Units.convert(oddLat, Units.Angle.TURN, Units.Angle.RADIAN)), 2));
            if ((Double.isNaN(A0) ? 1 : (int) Math.floor((Math.PI * 2) / A0)) != (Double.isNaN(A1) ? 1 : (int) Math.floor((Math.PI * 2) / A1))) { // if the two values are not equal, then the plane has crossed a longitude zone
                return null;
            }
            nLong[0] = Double.isNaN(A0) ? 1 : (int) Math.floor((Math.PI * 2) / A0);
            nLong[1] = nLong[0] - 1;
            double widthLong = 1d/nLong[mostRecent];
            int longitude = (int) Math.rint(x0 * nLong[1] - x1 * nLong[0]);
            longitude = (longitude < 0 ? longitude + nLong[mostRecent] : longitude);
            actualLong = (nLong[0] == 1) ? ((mostRecent==0)?x0:x1) : (widthLong * (longitude + ((mostRecent==0)?x0:x1))); // if nLong is 1, then the longitude is completely determined by x0 or x1
        }

        return checkValidityAndReturn(actualLong, (mostRecent==0)?evenLat:oddLat);
    }

    /**
     * Checks if the given latitude and longitude are valid and returns a GeoPos object if they are with rounded values of T32 longitude and latitude.
     * @param longitude : longitude in turn
     * @param latitude : latitude in turn
     * @return a GeoPos object with rounded values of T32 longitude and latitude if the given latitude and longitude are valid, null otherwise
     */
    private static GeoPos checkValidityAndReturn(double longitude, double latitude) {
        if (!GeoPos.isValidLatitudeT32((int) Math.rint(Units.convert(latitude, Units.Angle.TURN, Units.Angle.T32)))) {
            return null;
        }
        return new GeoPos(
                (int) Math.rint(Units.convert(longitude, Units.Angle.TURN, Units.Angle.T32)),
                (int) Math.rint(Units.convert(latitude, Units.Angle.TURN, Units.Angle.T32))
        );
    }
}