package ch.epfl.javions.gui;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class TileManager {
    public static final int TILE_SIZE = 256; // the size of a tile in pixels
    private static final int MAX_CACHE_SIZE = 100; // maximum number of tiles in cache memory
    private final Map<TileId, Image> tiles = new LinkedHashMap<>(MAX_CACHE_SIZE + 1, .75F, false); // the cache memory
    private final String tileServerUrl; // the url of the tile server
    private final Path cacheDirectory; // the directory where the tiles are stored

    /**
     * This class represents the position and zoom level of a tile.
     * @param cacheDirectory the directory where the tiles are stored
     * @param tileServerUrl the url of the tile server
     */
    public TileManager(Path cacheDirectory, String tileServerUrl) {
        this.cacheDirectory = cacheDirectory.resolve(tileServerUrl);
        this.tileServerUrl = tileServerUrl;
        tiles.put(null, null); // this is to avoid the cache to be empty when we try to replace the least accessed element
    }

    /**
     * This method tries to find the tile image in the cache memory, on the drive and on the server in this order.
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        if (!(TileId.isValid(tileId.zoom, tileId.x, tileId.y))) return null;
        Image data;
        if ((data = findInMemory(tileId)) != null) {
            return data;
        } else if ((data = findOnDrive(tileId)) != null) {
            return data;
        } else if ((data = findOnServer(tileId)) != null) {
            return data;
        } else {
            return null;
        }
    }

    /**
     * This method tries to find the tile image on the server.
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    private Image findInMemory(TileId tileId) {
        if (tiles.containsKey(tileId)) {
            return tiles.get(tileId);
        }
        return null;
    }

    /**
     * This method stores the tile image in the cache memory.
     * @param tileId the position and zoom level of the tile
     * @param image the tile image
     */
    private void storeInMemory(TileId tileId, Image image) {
        if (tiles.containsKey(tileId)) return;
        if (tiles.size() >= MAX_CACHE_SIZE) {
            TileId leastUsed = tiles.keySet().iterator().next();
            if (leastUsed != null) {
                tiles.remove(leastUsed);
            }
        }
        tiles.put(tileId, image);
    }

    /**
     * This method searches for the tile image on the user drive cache.
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    private Image findOnDrive(TileId tileId) throws IOException {
        Path absolutePath = cacheDirectory.resolve(tileId.zoom + "/" + tileId.x + "/" + tileId.y + ".png");
        if (Files.exists(absolutePath)) {
            Image i = new Image(new ByteArrayInputStream(Files.readAllBytes(absolutePath)));
            storeInMemory(tileId, i);
            return i;
        }
        return null;
    }

    /**
     * This method stores the tile image on the user drive cache.
     * @param tileId the position and zoom level of the tile
     * @param image the tile image to be stored in byte[] format
     */
    private void storeOnDrive(TileId tileId, byte[] image) {
        Path absolutePath = cacheDirectory.resolve(tileId.zoom + "/" + tileId.x + "/" + tileId.y + ".png");
        createDirectoryIfItDoesntExist(absolutePath.getParent().getParent().getParent().getParent());
        createDirectoryIfItDoesntExist(absolutePath.getParent().getParent().getParent());
        createDirectoryIfItDoesntExist(absolutePath.getParent().getParent());
        createDirectoryIfItDoesntExist(absolutePath.getParent());
        try {
            Files.write(absolutePath, image);
        }
        catch(IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * This method searches for the tile image on the tile server.
     * @param tileId the position and zoom level of the tile
     * @return the tile image or null if the tile is not found
     */
    private Image findOnServer(TileId tileId) throws IOException {
        byte[] data;
        URLConnection connection = Objects.requireNonNull(urlForTileAt(tileId)).openConnection();
        connection.setRequestProperty("User-Agent", "Javions");
        try (InputStream i = connection.getInputStream()) {
            data = i.readAllBytes();
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
     * @param tileId the position and zoom level of the tile
     * @return the URL of the tile image
     */
    private URL urlForTileAt(TileId tileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://").append(tileServerUrl).append("/").append(tileId.zoom).append("/").append(tileId.x).append("/").append(tileId.y).append(".png");
        try {
            return new URL(sb.toString());
        }
        catch (MalformedURLException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * This method creates the directory if it doesn't exist. It is used to create the cache directory.
     * @param path the path of the directory
     */
    private void createDirectoryIfItDoesntExist(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            }
            catch(IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * This class represents the position and zoom level of a tile.
     */
    public record TileId(int zoom, long x, long y) {

        public TileId {
            if (!isValid(zoom, x, y)) {
                throw new IllegalArgumentException("Invalid tile id");
            }
        }
        public static boolean isValid(int zoom, long x, long y) {
            long maxIndex = 2L << zoom - 1; // equivalent to Math.pow(2, zoom) - 1 but faster
            return ((x > 0 && y > 0) &&
                    (6 <= zoom && zoom <= 19) &&
                    (x < maxIndex && y < maxIndex));
        }
    }
}