package ch.epfl.cs108;

/**
 * This class contains the tokens pushing to the EPFL's servers.
 * It doesn't matter if the tokens are uploaded to GitHub as they are only valid for a week.
 *
 * @author @franklintra (362694)
 * @project Javions
 */
public final class TOKENS {
    private TOKENS() {
    } // Prevents instantiation

    public static String TOKEN_1() {
        return System.getenv("TOKEN1");
    }

    public static String TOKEN_2() {
        return System.getenv("TOKEN2");
    }
}
