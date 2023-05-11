package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author @franklintra (362694)
 * @project Javions
 * This class is the controller for the MAP. It handles the drawing of the MAP and the event handlers for zooming and scrolling around.
 * It uses a TileManager to cache the tiles most recent tiles in Random Access Memory and optimize tiles loading time.
 * It uses a MapParameters to store the zoom level, the center of the MAP and the top left corner of the MAP.
 * You can get a Pane from this class that you can add to your scene in java fx.
 * Therefore, the main usage of this class is to
 * represent the usable MAP (with scroll, zoom etc) as a Pane Object for java fx.
 */
public class BaseMapController {
    /**
     * The tile manager for ram and disk caching and downloading from the internet
     */
    private final TileManager tiles;
    /**
     * The MAP parameters (zoom level, topLeft corner, etc.). It is used JavaFX bindings and to know where to draw the tiles.
     */
    private final MapParameters parameters;
    /**
     * The pane that contains the MAP (JavaFX object that can be added to a scene)
     */
    private final Pane mapPane;
    /**
     * The canvas that is drawn on (included in the MAP pane and resized with it)
     */
    private final Canvas canvas;
    /**
     * A boolean to optimize the app. If a parameters has been changed or the window has been resized, this turns true and the map is redrawn.
     * Otherwise, the map is not redrawn every frame which saves a lot of CPU time.
     */
    private boolean redrawNeeded;
    /**
     * The image of an empty tile (used to fill the MAP when the tiles are loading and/or a tile is missing)
     * It stores the grid as an Image and doesn't redraw it everytime because displaying an image is hardware-accelerated in java.
     */
    private Image gridImage = null;

    /**
     * The constructor to make a new BaseMapController.
     * It takes a tile manager and a map parameters object and stores them.
     * It also sets up the canvas and the pane.
     * It finally sets up the event handlers for JavaFX to be able to scroll and zoom.
     * @param tileProvider The tile manager for ram and disk caching
     * @param mapParameters The MAP parameters (zoom level, center, etc.)
     */
    public BaseMapController(TileManager tileProvider, MapParameters mapParameters) {
        this.tiles = tileProvider;
        this.parameters = mapParameters;
        this.canvas = new Canvas();
        this.mapPane = new Pane(canvas);
        setupEventHandlers(); // Set up the event handlers for JavaFX
    }

    /**
     * This method is a getter for the MAP pane
     * @return The MAP pane
     */
    public Pane getPane() {
        return mapPane;
    }

    /**
     * This method is used to center the MAP on a given position
     * todo: test it + should it center on the center of the MAP or the mouse position?
     * @param position The position to center on
     */
    public void centerOn(GeoPos position) {
        int zoomLevel = parameters.getZoomLevel();
        double xMercator = WebMercator.x(zoomLevel, position.longitude());
        double yMercator = WebMercator.y(zoomLevel, position.latitude());
        Point2D topLeft = parameters.getTopLeftCorner();
        Point2D center = new Point2D(getPane().getWidth() / (2*TileManager.TILE_SIZE), getPane().getHeight() / (2*TileManager.TILE_SIZE));
        parameters.scroll(xMercator - topLeft.getX(), yMercator - topLeft.getY());
        parameters.scroll(center.getX(), center.getY()); //todo: check if it is meant to be negative or not
    }

    /**
     * This method setup all event handlers for java FX to:
     * Resize the canvas with the pane to always keep the map the same size as the window
     * Handles zooming and scrolling around properly.
     */
    private void setupEventHandlers() {
        canvas.getGraphicsContext2D().setFill(Color.GRAY); // default drawing color
        canvas.widthProperty().bind(mapPane.widthProperty()); // to make the canvas resize with the pane
        canvas.heightProperty().bind(mapPane.heightProperty()); // to make the canvas resize with the pane
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        }); // This is to redraw the canvas only when the scene is ready and it is needed


        // Gestion des événements de la souris
        LongProperty minScrollTime = new SimpleLongProperty();
        mapPane.setOnScroll(e -> {
            int zoomDelta = (int) - Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            parameters.scroll(e.getX(), e.getY());
            parameters.changeZoomLevel(zoomDelta);
            parameters.scroll(-e.getX(), -e.getY());
            redrawOnNextPulse();
        });

        // Gestion des événements de redimensionnement
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());

        // Gestion des événements de glissement
        AtomicReference<Point2D> lastMousePos = new AtomicReference<>(new Point2D(0, 0)); // this is to be able to edit the content of the variable from inside the lambda
        mapPane.setOnMousePressed(e -> lastMousePos.set(new Point2D(e.getX(), e.getY())));
        mapPane.setOnMouseDragged(e -> {
            Point2D newMousePos = new Point2D(e.getX(), e.getY());
            Point2D delta = lastMousePos.get().subtract(newMousePos);

            parameters.scroll(delta.getX(), delta.getY());

            lastMousePos.set(newMousePos);
            redrawOnNextPulse();
        });
        //mapPane.setOnMouseReleased(e -> lastMousePos[0] = null);
    }

    /**
     * This method is used to redraw the canvas only if needed
     * It is called on every pulse of the scene (see setupEventHandlers)
     * It only actually redraws if the redrawNeeded boolean is true
     */
    private void redrawIfNeeded() {
        if (redrawNeeded) {
            draw();
            redrawNeeded = false;
        }
    }

    /**
     * This method is used to request a redraw on the next pulse of the scene and request a next pulse to the Platform (Operating System)
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * This method is used to draw the MAP on the canvas with the current parameters
     */
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // it is not necessary to clear the canvas because the tiles are drawn over each other.
        // It is slightly more optimized this way but only works because we are drawing a MAP.
        // If we were drawing a game for example, we would need to clear the canvas.
        //gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int zoom = parameters.getZoomLevel();
        Point2D topLeftMercator = parameters.getTopLeftCorner();

        int widthInTiles = (int) Math.ceil(getPane().getWidth() / TileManager.TILE_SIZE);
        int heightInTiles = (int) Math.ceil(getPane().getHeight() / TileManager.TILE_SIZE);

        long firstTileX = (long) Math.floor(topLeftMercator.getX() / TileManager.TILE_SIZE);
        long firstTileY = (long) Math.floor(topLeftMercator.getY() / TileManager.TILE_SIZE);

        long lastTileX = firstTileX + widthInTiles;
        long lastTileY = firstTileY + heightInTiles;


        // These imbricated loops draw the tiles on the canvas
        for (long x = firstTileX; x <= lastTileX; x++) {
            for (long y = firstTileY; y <= lastTileY; y++) {
                double tilePosX = x * TileManager.TILE_SIZE - topLeftMercator.getX();
                double tilePosY = y * TileManager.TILE_SIZE - topLeftMercator.getY();
                try {
                    Image tile = tiles.imageForTileAt(new TileManager.TileId(zoom, x, y));
                    gc.drawImage(tile, tilePosX, tilePosY);
                    // Draw the coordinates of the tile in the middle of the tile for debugging purposes
                    gc.fillText("(" + x + ", " + y + ")", tilePosX + TileManager.TILE_SIZE/2f, tilePosY + TileManager.TILE_SIZE/2f);
                } catch (IOException e) {
                    // The tile could not be drawn, draw a grid instead.
                    // In the future if we want to still be able to move around the map as the tiles are loading,
                    // we could draw the grid first and then draw the tiles over it when they are loaded for performance reasons.
                    drawGrid(gc, tilePosX, tilePosY);
                }
            }
        }
    }


    private void ensureGridImageCreated() {
        int gridSize = 256;
        int rectWidth = TileManager.TILE_SIZE;
        int rectHeight = TileManager.TILE_SIZE;

        if (gridImage == null) {
            Canvas canvas = new Canvas(rectWidth, rectHeight);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            // Draw vertical lines within the given rectangle
            for (int x = 0; x <= rectWidth; x += gridSize) {
                gc.strokeLine(x, 0, x, rectHeight);
            }
            // Draw horizontal lines within the given rectangle
            for (int y = 0; y <= rectHeight; y += gridSize) {
                gc.strokeLine(0, y, rectWidth, y);
            }
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            gridImage = canvas.snapshot(params, null);
        }
    }


    /**
     * This method is used to draw a grid on the canvas
     * @param gc the graphics context to draw on
     * @param rectX the x coordinate of the top left corner of the rectangle to draw the grid in
     * @param rectY the y coordinate of the top left corner of the rectangle to draw the grid in
     */
    private void drawGrid(GraphicsContext gc, double rectX, double rectY) {
        ensureGridImageCreated();
        gc.clearRect(rectX, rectY, TileManager.TILE_SIZE, TileManager.TILE_SIZE);
        gc.drawImage(gridImage, rectX, rectY);
    }
}
