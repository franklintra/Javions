package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;
import javafx.geometry.Point2D;

/**
 * @author @franklintra (362694)
 * @project Javions
 * This class is used to store the parameters of the MAP. It is used in BaseMapController.
 * @see BaseMapController
 * This is used to:
 * - store the zoom level of the MAP
 * - store the x and y coordinates of the top left corner of the MAP
 * - scroll around the MAP
 * - zoom in and out of the MAP
 */
public class MapParameters {
    /**
     * The minimum zoom level for this project
     * 6 is the minimum zoom level for this project (we will not zoom out further because the radio signal is too weak)
     */
    public static final int MIN_ZOOM = 6;
    /**
     * The maximum zoom level for this project
     * 19 is the maximum zoom level of OpenStreetMap hence the maximum zoom level for this project
     */
    public static final int MAX_ZOOM = 19;
    /**
     * The current zoom level of the MAP
     */
    private final IntegerProperty zoomLevel;
    /**
     * The x coordinate of the top left corner of the MAP in Web Mercator projection
     */
    private final DoubleProperty minX;
    /**
     * The y coordinate of the top left corner of the MAP in Web Mercator projection
     */
    private final DoubleProperty minY; // The y coordinate of the top left corner of the MAP

    /**
     * The constructor of the MapParameters class
     *
     * @param zoomLevel the zoom level of the MAP
     * @param minX      the x coordinate of the top left corner of the MAP
     * @param minY      the y coordinate of the top left corner of the MAP
     */
    public MapParameters(int zoomLevel, double minX, double minY) {
        Preconditions.checkArgument(MIN_ZOOM <= zoomLevel && zoomLevel <= MAX_ZOOM);
        this.zoomLevel = new SimpleIntegerProperty(zoomLevel);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
    }

    /**
     * A read-only property for the zoom level
     *
     * @return the read-only property
     */
    public ReadOnlyIntegerProperty zoomLevelProperty() {
        return zoomLevel;
    }

    /**
     * A getter for the zoom level
     *
     * @return the zoom level
     */
    public int getZoomLevel() {
        return zoomLevel.get();
    }

    /**
     * A read-only property for the x coordinate of the top left corner of the MAP
     *
     * @return the read-only property
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * A getter for the x coordinate of the top left corner of the MAP
     *
     * @return the x coordinate of the top left corner of the MAP
     */
    public double getMinX() {
        return minX.get();
    }

    /**
     * A read-only property for the y coordinate of the top left corner of the MAP
     *
     * @return the read-only property
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    /**
     * A getter for the y coordinate of the top left corner of the MAP
     *
     * @return the y coordinate of the top left corner of the MAP
     */
    public double getMinY() {
        return minY.get();
    }

    /**
     * A method to scroll the MAP
     *
     * @param deltaX the x coordinate of the top left corner of the MAP
     * @param deltaY the y coordinate of the top left corner of the MAP
     */
    public void scroll(double deltaX, double deltaY) {
        minX.set(minX.get() + deltaX);
        minY.set(minY.get() + deltaY);
    }

    /**
     * A method to get the top left corner of the MAP. It is entirely defined by minX and minY. This makes it easier to read the code in BaseMapController.
     *
     * @return the top left corner of the MAP as a Point2D in javaFX.
     * This specific public method was not required in the project statement but it is used in BaseMapController to make the code easier to read.
     */
    public Point2D getTopLeftCorner() {
        return new Point2D(minX.get(), minY.get());
    }

    /**
     * A method to change the zoom level of the MAP
     * It also changes the x and y coordinates of the top left corner of the MAP so that this corner still shows the same part of the MAP
     *
     * @param deltaZoom the change in zoom level (will always be 1 or -1;
     */
    public void changeZoomLevel(int deltaZoom) {
        int newZoom = Math2.clamp(MIN_ZOOM, zoomLevel.get() + deltaZoom, MAX_ZOOM);
        double scaleFactor = Math.scalb(1, newZoom - zoomLevel.get());
        minX.set(minX.get() * scaleFactor);
        minY.set(minY.get() * scaleFactor);
        zoomLevel.set(newZoom);
    }
}