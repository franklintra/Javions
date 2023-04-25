package ch.epfl.javions.gui;


import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class TileManagerTest {
    void testImageForTileAt() throws IOException {
        Path cache = Path.of("tile-cache");
        TileManager tileManager = new TileManager(cache, "tile.openstreetmap.org");
        TileManager.TileId tileId1 = new TileManager.TileId(17, 67927, 46357);
        Image image = tileManager.imageForTileAt(tileId1);
        tileManager.imageForTileAt(tileId1);
        tileManager.imageForTileAt(tileId1);
        ImageDisplayer.display(image);
    }
}