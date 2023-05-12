package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class AircraftTableControllerTest extends Application {
    private static final GeoPos maison = new GeoPos((int) Units.convert(2.2794736259693136, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(48.88790023468289, Units.Angle.DEGREE, Units.Angle.T32));
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(getClass().getResource("/aircraft.zip").getPath()));
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();

        var root = new BorderPane(new AircraftTableController(aircraftStateManager.states(), selectedAircraftState).getPane());
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TestBaseMapController");
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.show();
        long lastMessage = 0;
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() >= bytes.length) {
                long timeStampNs = s.readLong();
                /*long timeDifference = TimeUnit.NANOSECONDS.toMillis(timeStampNs - lastMessage);
                try {
                    Thread.sleep(timeDifference);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                lastMessage = timeStampNs;
                s.readNBytes(bytes, 0, bytes.length);
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs, message));
                if (m == null) continue;
                aircraftStateManager.updateWithMessage(m);
                aircraftStateManager.purge();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
