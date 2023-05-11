package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AircraftControllerTest extends Application {
    ObservableSet<ObservableAircraftState> st;
    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(String fileName)
            throws IOException {
        List<RawMessage> finalList = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() > bytes.length) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);

                finalList.add(new RawMessage(timeStampNs, message));
            }
        } catch (Exception e) {
            System.err.println("You mf");
        }
        return finalList;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TileManager tileManager = new TileManager(Path.of("tile-cache"), "tile.openstreetmap.org");
        MapParameters mp = new MapParameters(17, 17_389_327, 11_867_430);

        BaseMapController bmc = new BaseMapController(tileManager, mp);

        // Création de la base de données
        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = Path.of((dbUrl).toURI()).toString();
        var db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);
        st = asm.states();
        //AircraftTableController ac = new AircraftTableController(st, sap);
        //var root = new StackPane(bmc.getPane(), ac.getPane());
        primaryStage.setScene(new Scene(bmc.getPane()));
        primaryStage.show();

        var mi = readAllMessages("messages_20230318_0915.bin")
                .iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
                        Message m = MessageParser.parse(mi.next());
                        if (m != null) {
                            asm.updateWithMessage(m);
                            asm.purge();
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }
}