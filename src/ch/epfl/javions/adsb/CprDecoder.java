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
        int[] nLat = {60, 59};
        int[] nLong = new int[2];
        double[] widthLat = {1d/nLat[0], 1d/nLat[1]};
        double widthLong;
        double latZone;
        double longZone;
        double actualLat;
        double actualLong;

        { // calculate latitude zone and actual latitude
            int lat = (int) Math.rint(y0 * nLat[1] - y1 * nLat[0]); // this is a temporary value used to compute latZone0 and latZone1
            latZone = lat < 0 ? lat + nLat[mostRecent] : lat;
            actualLat = widthLat[mostRecent] * (latZone + ((mostRecent==0)?y0:y1));
        }

        { // calculate number of longitude zones, longitude zone and actual longitude
            double a = Math.acos(1 - (1 - Math.cos(Math.PI * 2 * widthLat[0])) / Math.pow(Math.cos(Units.convert(actualLat, Units.Angle.TURN, Units.Angle.DEGREE)), 2)); // this is a temporary value used to compute nLong0 and nLong1
            nLong[0] = Double.isNaN(a) ? 1 : (int) Math.floor(Math.PI * 2 / a);
            nLong[1] = Double.isNaN(a) ? 1 : nLong[0] - 1;
            widthLong = 1d/nLong[mostRecent];
            if (nLong[mostRecent] == 1) {
                actualLong = x0;
            }
            else {
                double longi = Math.rint(x0 * nLong[1] - x1 * nLong[0]);
                longZone = longi < 0 ? longi + nLong[mostRecent] : longi;
                actualLong = (widthLong * (longZone + ((mostRecent==0)?x0:x1)));
            }
        }

        if (Units.convert(actualLat, Units.Angle.TURN, Units.Angle.DEGREE) < -90 || Units.convert(actualLat, Units.Angle.TURN, Units.Angle.DEGREE) > 90 ||
                Units.convert(actualLong, Units.Angle.TURN, Units.Angle.DEGREE) < -90 || Units.convert(actualLong, Units.Angle.TURN, Units.Angle.DEGREE) > 90) {
            return null;
        }

        return new GeoPos(
                (int) Units.convert(actualLong, Units.Angle.TURN, Units.Angle.T32),
                (int) Units.convert(actualLat, Units.Angle.TURN, Units.Angle.T32)
        );
    }
}