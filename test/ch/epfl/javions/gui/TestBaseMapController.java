package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
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

    private static final GeoPos maison = new GeoPos((int) Units.convert(2.2794736259693136, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(48.88790023468289, Units.Angle.DEGREE, Units.Angle.T32));
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        Path tileCache = Path.of("tile-cache");
        TileManager tm =
                new TileManager(tileCache, "tile.openstreetmap.org");
        MapParameters mp =
                new MapParameters(17, 17_389_327, 11_867_430);
        BaseMapController bmc = new BaseMapController(tm, mp);
        var root = new BorderPane(bmc.getPane());
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TestBaseMapController");
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.show();
        bmc.centerOn(maison);
    }
}