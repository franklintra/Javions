package ch.epfl.javions.gui;

import ch.epfl.CONFIGURATION;
import javafx.application.Application;
import java.nio.file.Path;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public final class TestBaseMapController extends Application {

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, CONFIGURATION.MAP.TILE_SERVER_URL);
        MapParameters mp =
                new MapParameters(17, 17_389_327, 11_867_430);
        BaseMapController bmc = new BaseMapController(tm, mp);
        var root = new BorderPane(bmc.getPane());
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TestBaseMapController");
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.show();
        bmc.centerOn(CONFIGURATION.MAP.INITIAL_POSITION);
    }
}