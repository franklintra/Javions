package ch.epfl.javions.gui;


import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class TileManagerTest {
    @Test
    void testImageForTileAt() {
        Path cache = Path.of("/users/tranie/Desktop/cache/");
        TileManager tileManager = new TileManager(cache, "tile.openstreetmap.org");
        TileManager.TileId tileId1 = new TileManager.TileId(5, 2, 0);
        Image image = tileManager.imageForTileAt(tileId1);
        tileManager.imageForTileAt(tileId1);
        tileManager.imageForTileAt(tileId1);
        //ImageDisplayer.display(image);
    }
}
