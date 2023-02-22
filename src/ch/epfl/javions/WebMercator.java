package ch.epfl.javions;

public class WebMercator {
    private WebMercator() {}

    /**
     * @param zoomLevel the zoom level
     * @param longitude the longitude in radians
     * @return the x coordinate on the map
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(1, 8+zoomLevel) * (longitude/(2*Math.PI) + 0.5f);
    }
    /**
     * @param zoomLevel the zoom level
     * @param latitude the latitude in radians
     * @return the y coordinate on the map
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(1, 8+zoomLevel) * (-Math2.asinh(Math.tan(latitude)) / (2*Math.PI) + 0.5f);
    }
}
