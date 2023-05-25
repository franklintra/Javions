package ch.epfl.javions.gui;

import javafx.scene.paint.Color;

/**
 * @author @chukla (357550)
 * @project Javions
 * The ColorRamp class represents a color gradient or ramp defined by a series of colors.
 * It allows retrieving the color at a specific altitude along the gradient.
 */
public final class ColorRamp {
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private final Color[] colours;

    /**
     * Constructs a ColorRamp with the specified colors passed through the constructor.
     *
     * @param steps The colors defining the gradient.
     * @throws IllegalArgumentException if there are less than two colors specified.
     */
    public ColorRamp(Color... steps) {
        if (steps.length < 2) {
            throw new IllegalArgumentException();
        }
        this.colours = steps.clone();
    }

    /**
     * Returns the color at the specified position based on the altitude along the color gradient.
     *
     * @param value The position along the gradient which is based on the altitude of the aircraft, ranging from 0 to 1.
     * @return The color at the specified position.
     */
    public Color at(double value) {
        if (value <= 0) {
            return colours[0];
        } else if (value >= 1) {
            return colours[colours.length - 1];
        } else {
            double segmentSize = 1.0 / (colours.length - 1);
            int i = (int) (value / segmentSize);
            double fraction = (value - (i * segmentSize)) / segmentSize;
            return colours[i].interpolate(colours[i + 1], fraction);
        }
    }
}


