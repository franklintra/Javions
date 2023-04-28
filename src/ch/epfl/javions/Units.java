package ch.epfl.javions;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

/**
 * This class contains constants and methods for unit conversions.
 * The methods are static and the class is not instantiable.
 */
public final class Units {
    public static final double CENTI = 1e-2;
    public static final double KILO = 1e3;

    /**
     * All constructors are private to prevent instantiation.
     * Everything is static and is pretty self-explanatory.
     */
    private Units() {
    }

    /**
     * @param value value to be converted
     * @param from  unit of the value
     * @param to    unit to convert to
     * @return the converted value
     */
    public static double convert(double value, double from, double to) {
        return value * (from / to);
    }

    /**
     * @param value value to be converted
     * @param from  unit of the value
     * @return the converted value
     */
    public static double convertFrom(double value, double from) {
        return value * from;
    }

    /**
     * @param value value to be converted
     * @param to    unit to convert to
     * @return the converted value
     */
    public static double convertTo(double value, double to) {
        return value * (1 / to);
    }

    /**
     * Angle units.
     * The constants are the number of radians in one unit.
     * For example, 1 radian is 1 radian, 1 turn is 2π radians, 1 degree is 2π/360 radians, etc.
     */
    public static final class Angle {
        /**
         * Default angle unit is radian
         */
        public static final double RADIAN = 1.0;
        /**
         * Value of a turn in radians
         */
        public static final double TURN = 2 * Math.PI * RADIAN;
        /**
         * Value of a degree in radians
         */
        public static final double DEGREE = TURN / 360;
        /**
         * Value of a T32 in radians
         */
        public static final double T32 = TURN / Math.scalb(1, 32);

        private Angle() {
        }

    }

    /**
     * Time units.
     * The constants are the number of seconds in one unit.
     * For example, 1 second is 1 second, 1 minute is 60 seconds, 1 hour is 3600 seconds, etc.
     */
    public static final class Time {
        /**
         * Default time unit is second
         */
        public static final double SECOND = 1.0;
        /**
         * Value of a minute in seconds
         */
        public static final double MINUTE = 60 * SECOND;
        /**
         * Value of an hour in seconds
         */
        public static final double HOUR = 60 * MINUTE;

        private Time() {
        }
    }

    /**
     * Length units.
     * The constants are the number of meters in one unit.
     * For example, 1 meter is 1 meter, 1 centimeter is 1/100 meter, 1 inch is 2.54 centimeters, etc.
     */
    public static final class Length {
        /**
         * Default length unit is meter
         */
        public static final double METER = 1.0;
        /**
         * Value of a centimeter in meters
         */
        public static final double CENTIMETER = CENTI * METER;
        /**
         * Value of an inch in meters
         */
        public static final double INCH = CENTIMETER * 2.54;
        /**
         * Value of a foot in meters
         */
        public static final double FOOT = INCH * 12;
        /**
         * Value of a yard in meters
         */
        public static final double KILOMETER = KILO * METER;
        /**
         * Value of a nautical mile in meters
         */
        public static final double NAUTICAL_MILE = METER * 1852;

        private Length() {
        }
    }

    /**
     * Speed units.
     * The constants are the number of meters per second in one unit.
     * For example, 1 meter per second is 1 meter per second, 1 kilometer per hour is 1/3.6 meters per second, 1 knot is 1852/3600 meters per second, etc.
     */
    public static final class Speed {
        /**
         * Value of a kilometer per hour in meters per second
         */
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        /**
         * Value of a knot in meters per second
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;

        private Speed() {
        }
    }
}
