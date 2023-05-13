package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.application.Platform;
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
    private final AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(Objects.requireNonNull(getClass().getResource("/aircraft.zip")).getPath()));

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();

        Thread messageDecoding = new Thread(this::readAndProcessMessages);

        Platform.runLater(() -> {
            var root = new BorderPane(new AircraftTableController(aircraftStateManager.states(), selectedAircraftState).getPane());
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("TestBaseMapController");
            primaryStage.setWidth(1920);
            primaryStage.setHeight(1080);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        });
        messageDecoding.start();
    }

    private void readAndProcessMessages() {
        long lastMessageTimeStampNs = 0;
        long lastTime = 0;
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (s.available() >= bytes.length) {
                // Sleep to simulate real time
                long timeStampNs = s.readLong();
                long currentTime = System.nanoTime();
                long messageTimeDifference = TimeUnit.NANOSECONDS.toMillis(timeStampNs - lastMessageTimeStampNs);
                long programTimeDifference = TimeUnit.NANOSECONDS.toMillis(currentTime - lastTime);
                if (messageTimeDifference > programTimeDifference) {
                    try {
                        Thread.sleep(messageTimeDifference-programTimeDifference);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                lastTime = currentTime;
                lastMessageTimeStampNs = timeStampNs;
                // End of sleep to simulate real time
                s.readNBytes(bytes, 0, bytes.length);
                Message m = MessageParser.parse(new RawMessage(timeStampNs, new ByteString(bytes)));
                if (Objects.isNull(m)) continue;
                aircraftStateManager.updateWithMessage(m);
                aircraftStateManager.purge();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
