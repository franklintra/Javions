package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class BaseMapController {
    private final TileManager tiles; // The tile manager for ram and disk caching
    private final MapParameters parameters; // The map parameters
    private final Pane mapPane; // The pane that contains the map
    private final Canvas canvas; // The canvas that is drawn on
    private boolean redrawNeeded; // Whether redrawing is needed or not (optimization purposes)

    /**
     * The constructor to make a new BaseMapController
     * @param tileProvider The tile manager for ram and disk caching
     * @param mapParameters The map parameters (zoom level, center, etc.)
     */
    public BaseMapController(TileManager tileProvider, MapParameters mapParameters) {
        this.tiles = tileProvider;
        this.parameters = mapParameters;
        this.canvas = new Canvas();
        this.mapPane = new Pane(canvas);

        canvas.getGraphicsContext2D().setFill(Color.GRAY);

        canvas.widthProperty().bind(mapPane.widthProperty()); // to make the canvas resize with the pane
        canvas.heightProperty().bind(mapPane.heightProperty()); // to make the canvas resize with the pane
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        }); // This is to redraw the canvas only when the scene is ready and it is needed

        setupEventHandlers(); // Set up the event handlers for JavaFX
    }

    /**
     * This method is a getter for the map pane
     * @return The map pane
     */
    public Pane getPane() {
        return mapPane;
    }

    /**
     * This method is used to center the map on a given position
     * todo: test it + should it center on the center of the map or the mouse position?
     * @param position The position to center on
     */
    public void centerOn(GeoPos position) {
        long centerX = (long) ((parameters.getMinX() + getPane().getHeight())/2);
        long centerY = (long) (parameters.getMinY() + getPane().getWidth()/2);
        parameters.scroll(
                centerX - WebMercator.x(position.longitudeT32(), parameters.getZoomLevel()),
                centerY - WebMercator.y(position.latitudeT32(), parameters.getZoomLevel())
        );
    }

    /**
     * This method setup all event handlers for java FX to handle zooming and scrolling around properly.
     */
    private void setupEventHandlers() {
        // Gestion des événements de la souris
        LongProperty minScrollTime = new SimpleLongProperty();
        mapPane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int previousZoom = parameters.getZoomLevel();
            parameters.changeZoomLevel(zoomDelta);
            if (!(previousZoom == parameters.getZoomLevel())) {
                parameters.scroll(e.getX(), e.getY());
            }
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
        if (!redrawNeeded) return;
        redrawNeeded = false;
        draw();
    }

    /**
     * This method is used to request a redraw on the next pulse of the scene and request a next pulse
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * This method is used to draw the map on the canvas with the current parameters
     */
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // it is not necessary to clear the canvas because the tiles are drawn over each other.
        // It is slightly more optimized this way but only works because we are drawing a map.
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

        //System.out.println("First tile: (" + firstTileX + ", " + firstTileY + ")");
        //System.out.println("Last tile: (" + lastTileX + ", " + lastTileY + ")");
        //System.out.println("Area (tiles) = " + (lastTileX - firstTileX + 1) * (lastTileY - firstTileY + 1));

        for (long x = firstTileX; x <= lastTileX; x++) {
            for (long y = firstTileY; y <= lastTileY; y++) {
                double tilePosX = x * TileManager.TILE_SIZE - topLeftMercator.getX();
                double tilePosY = y * TileManager.TILE_SIZE - topLeftMercator.getY();
                try {
                    if (!(TileManager.TileId.isValid(zoom, x, y))) {
                        throw new IOException("Invalid tile");
                    }
                    Image tile = tiles.imageForTileAt(new TileManager.TileId(zoom, x, y));
                    gc.drawImage(tile, tilePosX, tilePosY);
                    gc.fillText("(" + x + ", " + y + ")", tilePosX + TileManager.TILE_SIZE/2f, tilePosY + TileManager.TILE_SIZE/2f);
                } catch (IOException e) {
                    // The tile could not be drawn, draw a grid instead. This allows us to not clearRect the canvas every frame we redraw.
                    drawGrid(gc, tilePosX, tilePosY);
                }
            }
        }
    }

    /**
     * This method is used to draw a grid on the canvas
     * @param gc the graphics context to draw on
     * @param rectX the x coordinate of the top left corner of the rectangle to draw the grid in
     * @param rectY the y coordinate of the top left corner of the rectangle to draw the grid in
     */
    private void drawGrid(GraphicsContext gc, double rectX, double rectY) {
        int gridSize = 256;
        int rectWidth = TileManager.TILE_SIZE;
        int rectHeight = TileManager.TILE_SIZE;

        gc.clearRect(rectX, rectY, rectWidth, rectHeight);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        // Draw vertical lines within the given rectangle
        for (double x = rectX; x <= rectX + rectWidth; x += gridSize) {
            gc.strokeLine(x, rectY, x, rectY + rectHeight);
        }

        // Draw horizontal lines within the given rectangle
        for (double y = rectY; y <= rectY + rectHeight; y += gridSize) {
            gc.strokeLine(rectX, y, rectX + rectWidth, y);
        }
    }
}
