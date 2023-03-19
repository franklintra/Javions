package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;

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
        int nLat0 = 60;
        int nLat1 = 59;
        int numberofZonesOfLongitude;

        double A = Math.acos(1- (1-Math.cos(Math.PI*2*1/nLat0)/Math.pow(Math.cos(y0), 2)));
        if (Double.isNaN(A)) {
            numberofZonesOfLongitude = 1;
        }
        else {
            numberofZonesOfLongitude = (int) Math.floor((2*Math.PI)/A);
        }

        if (mostRecent == 0) {
            //decoupage pair
            numberofZonesOfLongitude--;
        }
        else if (mostRecent == 1) {
            //decoupage impair
        }
        return null;
    }
}