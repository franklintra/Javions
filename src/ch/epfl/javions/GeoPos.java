package ch.epfl.javions;


/**
 * @author @franklintra
 * @project Javions
 */

public record GeoPos(int longitudeT32, int latitudeT32) {
    public GeoPos {
        if (!isValidLatitudeT32(latitudeT32)) {
            throw new IllegalArgumentException("Latitude is not valid: " + latitudeT32);
        }
    }

    /**
     * @param latitudeT32: the current latitude in T32 format
     * @return true iff the latitude is valid.
     * @throws IllegalArgumentException if the latitude is not valid.
     */
    public static boolean isValidLatitudeT32(int latitudeT32) throws IllegalArgumentException {
        return (-Math.pow(2, 30) <= latitudeT32) && (latitudeT32 <= Math.pow(2, 30));
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
    //todo: probably will need to add setters to update the positions according to the real-time data in the future
}

