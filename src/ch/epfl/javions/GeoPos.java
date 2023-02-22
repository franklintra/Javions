package ch.epfl.javions;

public class GeoPos {
    private int longitudeT32;
    private int latitudeT32;
    public GeoPos(int longitudeT32, int latitudeT32) {
        this.longitudeT32 = longitudeT32;
        this.latitudeT32 = latitudeT32;
    }

    /**
     * @param latitudeT32
     * @return true iff the latitude is valid.
     * @throws IllegalArgumentException if the latitude is not valid.
     */
    public static boolean isValidLatitudeT32(int latitudeT32) throws IllegalArgumentException{
        if (latitudeT32 >= -Math.scalb(1, 30) && latitudeT32 <= Math.scalb(1, 30)) {
            return true;
        } else {
            throw new IllegalArgumentException("Latitude is not valid: " + latitudeT32);
        }
    }

    /**
     * @return the longitude in radians.
     */
    public double longitude() {
        return Units.convertTo(longitudeT32, Units.Angle.RADIAN);
    }

    /**
     * @return the latitude in radians.
     */
    public double latitude() {
        return Units.convertTo(latitudeT32, Units.Angle.RADIAN);
    }
}
