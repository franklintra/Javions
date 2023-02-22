package ch.epfl.javions;

public final class Units {
    /**
     * Private constructor to prevent instantiation.
     * Everything is static and is pretty self-explanatory.
     */
    private Units() {}
    public static final double CENTI = 1e-2;
    public static final double KILO = 1e3;

    public static class Angle {
        private Angle() {}
        public static final double RADIAN = 1.0;
        public static final double DEGREE = RADIAN*180/Math.PI;
        public static final double TURN = 2*Math.PI*RADIAN;
        public static final double T32 = TURN/Math.scalb(1, 32);

    }
    public static class Length {
        private Length() {}
        public static final double METER = 1.0;
        public static final double KILOMETER = KILO*METER;
        public static final double CENTIMETER = CENTI*METER;
        public static final double INCH = CENTIMETER*2.54;
        public static final double FOOT = INCH*12;
        public static final double NAUTICAL_MILE = METER*1852;
    }

    public static class Time {
        private Time() {}
        public static final double SECOND = 1.0;
        public static final double MINUTE = 60*SECOND;
        public static final double HOUR = 60*MINUTE;


    }
    public static class Speed {
        private Speed() {}
        public static final double METER_PER_SECOND = 1.0;
        public static final double KILOMETER_PER_HOUR = METER_PER_SECOND * (Time.HOUR / KILO);
        public static final double KNOT = METER_PER_SECOND * ((Length.NAUTICAL_MILE / Length.METER) * (Time.HOUR / Time.SECOND));
    }

    public static double convert(double value, double from, double to) {
        return value * from / to;
    }
    public static double convertFrom(double value, double from) {
        return value / from;
    }
    public static double convertTo(double value, double to) {
        return value * to;
    }
}
