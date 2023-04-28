package ch.epfl.javions;


/**
 * @author @franklintra (362694)
 * @author @chukla (357550)
 * @project Javions
 */

public record GeoPos(int longitudeT32, int latitudeT32) {
    /**
     * @param longitudeT32: the current longitude in T32 format
     * @param latitudeT32:  the current latitude in T32 format
     * @throws IllegalArgumentException if the longitude or latitude is not valid.
     */

    private final static int MAX_ABSOLUTE_LATITUDE_T32 = 1 << 30;

    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * @param latitudeT32: the current latitude in T32 format
     * @return true iff the latitude is valid.
     * @throws IllegalArgumentException if the latitude is not valid.
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return (-MAX_ABSOLUTE_LATITUDE_T32 <= latitudeT32) && (latitudeT32 <= MAX_ABSOLUTE_LATITUDE_T32);
    }

    /**
     * @return the longitude in radians.
     */
    public double longitude() {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * @return the latitude in radians.
     */
    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     * @return the textual representation of longitude and latitude to Degree
     */
    @Override
    public String toString() {
        return "(" +
                Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) +
                "°, " + Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) +
                "°)";
    }
}