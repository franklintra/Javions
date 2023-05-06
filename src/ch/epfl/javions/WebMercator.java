package ch.epfl.javions;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

public final class WebMercator {
    private WebMercator() {
    } // Prevents instantiation

    /**
     * @param zoomLevel the zoom level
     * @param longitude the longitude in radians
     * @return the x coordinate on the MAP
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(
                Units.convertTo(longitude, Units.Angle.TURN) + 0.5f,
                8 + zoomLevel
        );
    }

    /**
     * @param zoomLevel the zoom level
     * @param latitude  the latitude in radians
     * @return the y coordinate on the MAP
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(
                Units.convertTo(-Math2.asinh(Math.tan(latitude)),
                        Units.Angle.TURN) + 0.5f, 8 + zoomLevel
        );
    }
}