package ch.epfl.javions.gui;

import ch.epfl.CONFIGURATION;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 * This class manages the tiles of the map. It is used to:
 * - download the ones not already downloaded to the disk
 * - cache the most recently used ones in memory
 * @see BaseMapController
 */
public class TileManager {
    /**
     * This is the size of the side of a tile in pixels.
     */
    public static final int TILE_SIZE = 256;
    /**
     * This is the max size of the cache memory in tiles (not bytes).
     * It is stored as an attribute, so we can change it easily.
     *
     * @see CONFIGURATION
     */
    private static final int MAX_CACHE_SIZE = 100;
    /**
     * This is the cache memory of the tiles. (For a given tile id, it stores the corresponding tile image)
     * it is a LinkedHashMap, so we can easily remove the least recently used element.
     *
     * @see LinkedHashMap
     */
    private final Map<TileId, Image> tiles = new LinkedHashMap<>(MAX_CACHE_SIZE, .75F, false);
    /**
     * This is the url of the tile server from where to download the tiles. (For example: "tile.openstreetmap.org")
     */
    private final String tileServerUrl;
    /**
     * This is the extension of the tile files. By default, if using the openstreetmap tile server, it is ".png".
     */
    private final String tileFormat = ".png";
    /**
     * This is the directory where the downloaded tiles need to be stored.
     * (they will actually be stored in a subdirectory of this directory named after the tile server url)
     * For example: ~/Javions/tile-cache/tile.openstreetmap.org/
     */
    private final Path cacheDirectory;

    /**
     * This is the constructor of the class.
     * It initializes the cache directory and the tile server url according to the parameters.
     *
     * @param cacheDirectory the directory where the tiles are stored
     * @param tileServerUrl  the url of the tile server
     */
    public TileManager(Path cacheDirectory, String tileServerUrl) {
        this.cacheDirectory = cacheDirectory.resolve(tileServerUrl);
        this.tileServerUrl = tileServerUrl;
    }

    /**
     * This method tries to find the tile image respectively :
     * - in the cache memory
     * - on the drive
     * - on the server
     *
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        if (!(TileId.isValid(tileId.zoom, tileId.x, tileId.y))) {
            throw new IOException("Invalid tile id");
        }
        Image data;
        if (Objects.nonNull(data = findInMemory(tileId))) {
            return data;
        }
        if (Objects.nonNull(data = findOnDriveAndStore(tileId))) {
            return data;
        }
        if (Objects.nonNull(data = downloadAndStore(tileId))) {
            return data;
        }
        throw new IOException("Tile not found, no error was thrown");
    }

    /**
     * This method stores the tile image in the cache memory.
     *
     * @param tileId the position and zoom level of the tile
     * @param image  the tile image
     */
    private void storeInMemory(TileId tileId, Image image) {
        if (tiles.containsKey(tileId)) {
            tiles.remove(tileId); // this is to make sure the tile is the most recently used
        } else if (tiles.size() >= MAX_CACHE_SIZE) {
            Iterator<TileId> it = tiles.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        tiles.put(tileId, image);
    }

    /**
     * This method stores the tile image on the user drive cache.
     *
     * @param tileId the position and zoom level of the tile
     * @param image  the tile image to be stored in byte[] format
     * @see TileManager
     */
    private void storeOnDrive(TileId tileId, byte[] image) throws IOException {
        Path tilePath = cacheDirectory
                .resolve(String.valueOf(tileId.zoom))
                .resolve(Long.toString(tileId.x))
                .resolve(tileId.y + tileFormat);
        Files.createDirectories(tilePath.getParent()); // create the directories if they don't exist all the way up to the file
        Files.write(tilePath, image);
    }

    /**
     * This method tries to find the tile image on the server.
     *
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    private Image findInMemory(TileId tileId) {
        return tiles.get(tileId);// this is because a LinkedHashMap accepts a null key hence we don't need to check if the key exists
    }

    /**
     * This method searches for the tile image on the user drive cache.
     * This also stores the tile image in the cache memory.
     *
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     * @see TileManager
     */
    private Image findOnDriveAndStore(TileId tileId) throws IOException {
        Path absolutePath = cacheDirectory
                .resolve(Integer.toString(tileId.zoom))
                .resolve(Long.toString(tileId.x))
                .resolve(tileId.y + tileFormat);
        if (Files.exists(absolutePath)) {
            try {
                Image i;
                try (InputStream data = Files.newInputStream(absolutePath)) {
                    i = new Image(data);
                    storeInMemory(tileId, i);
                }
                return i;
            } catch (IOException e) {
                throw new IOException("The image could not be read from the drive.", e);
            }
        }
        return null;
    }

    /**
     * This method searches for the tile image on the tile server.
     * This also stores the tile image in the cache memory and on the user drive cache.
     *
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     * @see TileManager
     */
    private Image downloadAndStore(TileId tileId) throws IOException {
        URL tileUrl = urlForTileAt(tileId);
        if (tileUrl == null) {
            return null;
        }
        byte[] data;
        try {
            URLConnection connection = tileUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Javions");
            try (InputStream i = connection.getInputStream()) {
                data = i.readAllBytes();
            }
        } catch (IOException e) {
            // this is because the tile server did not respond in time (or at all)
            // this is not an error and might happen because of internet connection issues or server overload
            // it could also be because the tile server does not have the specific tile
            throw new IOException("The tile server did not respond in time (or at all)", e);
        }
        if (data != null) {
            Image i = new Image(new ByteArrayInputStream(data));
            storeOnDrive(tileId, data);
            storeInMemory(tileId, i);
            return i;
        }
        return null;
    }

    /**
     * This method creates the URL of the tile image on the tile server.
     *
     * @param tileId the position and zoom level of the tile
     * @return the URL of the tile image
     */
    private URL urlForTileAt(TileId tileId) {
        String url = String.format("https://%s/%d/%d/%d%s", tileServerUrl, tileId.zoom, tileId.x, tileId.y, tileFormat);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * This immutable record represents the position and zoom level of a tile.
     * It is used to identify a tile.
     *
     * @see TileManager#imageForTileAt(TileId)
     */
    public record TileId(int zoom, long x, long y) {
        /**
         * This method checks if the tile id is valid.
         *
         * @param zoom the zoom level of the tile
         * @param x    the x position of the tile
         * @param y    the y position of the tile
         * @return true if the tile's attribute are valid, false otherwise
         */
        public static boolean isValid(int zoom, long x, long y) {
            long maxIndex = 2L << zoom - 1; // equivalent to Math.pow(2, zoom) - 1 but faster
            return ((x > 0 && y > 0) &&
                    (MapParameters.MIN_ZOOM <= zoom && zoom <= MapParameters.MAX_ZOOM) &&
                    (x < maxIndex && y < maxIndex));
        }
    }
}