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
    public GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);
        int[] nLat = {60, 59};
        int[] nLong = new int[2];
        double[] sigmaLat = {1d/nLat[0], 1d/nLat[1]};
        double[] sigmaLong = new double[2];
        int[] latZone = new int[2];
        int[] longZone = new int[2];
        int[] actualLat = new int[2];
        int[] actualLong = new int[2];
        { // calculate number of longitude zones, longitude zone and actual longitude
            double A = Math.acos(1 - (1 - Math.cos(Math.PI * 2 * sigmaLat[0]) / Math.pow(Math.cos(y0), 2))); // this is a temporary value used to compute nLong0 and nLong1
            nLong[0] = Double.isNaN(A) ? 1 : (int) Math.floor(Math.PI * 2 / A);
            nLong[1] = Double.isNaN(A) ? 1 : nLong[0] - 1;
            if (!Double.isNaN(A)) {
                int longi = (int) Math.rint(x0 * nLong[1] - x1 * nLong[0]);
                longZone[0] = longi < 0 ? longi + nLong[0] : longi;
                longZone[1] = longi < 0 ? longi + nLong[1] : longi;
            }
            sigmaLong[0] = 1d/nLong[0];
            sigmaLong[1] = 1d/nLong[1];
            actualLong[0] = (int) (sigmaLong[0] * (longZone[0] + x0));
            actualLong[1] = (int) (sigmaLong[1] * (longZone[1] + x1));
        }
        { // calculate latitude zone and actual latitude
            int lat = (int) Math.rint(y0 * nLat[1] - y1 * nLat[0]); // this is a temporary value used to compute latZone0 and latZone1
            latZone[0] = lat < 0 ? lat + nLat[0] : lat;
            latZone[1] = lat < 0 ? lat + nLat[1] : lat;
            actualLat[0] = (int) (sigmaLat[0] * (latZone[0] + y0));
            actualLat[1] = (int) (sigmaLat[1] * (latZone[1] + y1));
        }
        return new GeoPos(
                (int) Units.convert(actualLong[mostRecent], Units.Angle.TURN, Units.Angle.T32),
                (int) Units.convert(actualLat[mostRecent], Units.Angle.TURN, Units.Angle.T32)
        );
    }
}