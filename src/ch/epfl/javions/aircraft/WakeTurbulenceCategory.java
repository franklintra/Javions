package ch.epfl.javions.aircraft;

/**
 * @author @franklintra
 * @project Javions
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    public static WakeTurbulenceCategory fromString(String s) {
        switch (s) {
            case "L": return LIGHT;
            case "M": return MEDIUM;
            case "H": return HEAVY;
            default: return UNKNOWN;
        }
    }
}