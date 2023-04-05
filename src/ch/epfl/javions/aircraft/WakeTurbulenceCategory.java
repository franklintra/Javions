package ch.epfl.javions.aircraft;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    /**
     * @param s description to be converted to a WakeTurbulenceCategory
     * @return the WakeTurbulenceCategory corresponding to the description
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}