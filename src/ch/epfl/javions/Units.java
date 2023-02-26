package ch.epfl.javions;

/**
 * @project Javions
 * @author @franklintra
 */

public final class Units {
    public static final double CENTI = 1e-2;
    public static final double KILO = 1e3;
    /**
     * Private constructor to prevent instantiation.
     * Everything is static and is pretty self-explanatory.
     */
    private Units() {}

    public static double convert(double value, double from, double to) {
        return value * (from / to);
    }

    public static double convertFrom(double value, double from) {
        return value * from;
    }

    public static double convertTo(double value, double to) {
        return value * (1/to);
    }

    public static class Angle {
        public static final double RADIAN = 1.0;
        public static final double TURN = 2*Math.PI*RADIAN;
        public static final double DEGREE = TURN/360;
        public static final double T32 = TURN/Math.scalb(1, 32);
        private Angle() {}

    }

    public static class Time {
        public static final double SECOND = 1.0;
        public static final double MINUTE = 60*SECOND;
        public static final double HOUR = 60*MINUTE;
        private Time() {}
    }

    public static class Length {
        public static final double METER = 1.0;
        public static final double CENTIMETER = CENTI*METER;
        public static final double INCH = CENTIMETER*2.54;
        public static final double FOOT = INCH*12;
        public static final double KILOMETER = KILO*METER;
        public static final double NAUTICAL_MILE = METER*1852;
        private Length() {}
    }

    public static class Speed {
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        private Speed() {}
    }
}
