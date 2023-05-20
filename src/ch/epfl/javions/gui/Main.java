package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author @franklintra (362694)
 * @project Javions
 * The Main class contains the main program. It extends Application, and has a main method
 * that does nothing but call launch.
 */
public final class Main extends Application {
    // Configuration
    private static final Path tileCache = Path.of("tile-cache");

    // End of configuration

    /**
     * This field contains access to the database of all aircrafts.
     * It is polled to get additional aircraft's information.
     */
    private static final AircraftDatabase database = new AircraftDatabase(Objects.requireNonNull(Main.class.getResource("/aircraft.zip")).getPath());
    /**
     * The manager of the aircraft states. It gives access to currently visible aircraft states.
     */
    private static final AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
    /**
     * The selectedAircraftState property is an object property that contains the currently selected aircraft state.
     * It is used to sync the aircraft state selected in the table and the map.
     */
    private static final ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();

    /**
     * The messageQueue field is a concurrent linked queue that contains the messages
     * sampled and decoded in real time from the input stream (messages.bin or radio)
     */
    private static final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    /**
     * The messageCount property is a simple long property that counts the number of messages and is drawn on the status line.
     */
    private final SimpleLongProperty messageCount = new SimpleLongProperty(0);

    // The following fields are the controllers of the different parts of the GUI and their necessary parameters.

    /**
     * The tm field is a TileManager that manages the tiles of the map. It is used to create the BaseMapController.
     * @see BaseMapController
     */
    TileManager tm = new TileManager(tileCache, "tile.openstreetmap.org");
    /**
     * The mp field is a MapParameters that defines the parameters of the map. It is used to create the BaseMapController.
     * It contains zoom level and allows for scrolling.
     * Its default value is zoom level 8, with the top left corner of the map at the coordinates (33530, 23070).
     * @see BaseMapController
     */
    MapParameters mp = new MapParameters(8, 33530, 23070);
    /**
     * The map field is the controller of the map. It is used to create the map pane.
     */
    BaseMapController map = new BaseMapController(tm, mp);
    /**
     * The ac field is the controller of the overlay of the map.
     * It is used to draw trajectories, aircrafts and labels.
     */
    AircraftController mapOverlay = new AircraftController(mp, aircraftStateManager.states(), selectedAircraftState);
    /**
     * The table field is the controller of the aircraft table. It is used to create the aircraft table pane.
     */
    AircraftTableController table = new AircraftTableController(aircraftStateManager.states(), selectedAircraftState);
    /**
     * The statusLineController field is the controller of the status line (middle of the window).
     * It shows the number of visible aircraft and the number of messages received.
     */
    StatusLineController statusLineController = new StatusLineController();

    /**
     * The main method of the program.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method starts the application by showing the main gui,
     * starting the thread responsible for getting the messages,
     * and finally overriding part of the "animation timer" responsible for updating the aircraft states
     * based on the received messages. It purges the aircraft state manager every second.
     * It also adds behaviour on double click on the table to center the map on the selected aircraft.
     *
     * @see #mainScene() the construction of the main #Scene object.
     * @see AnimationTimer the animation timer responsible for updating the aircraft states based on the received messages.
     * @see AircraftStateManager#purge() # purge the aircraft state manager every second in the AnimationTimer.
     * @see AircraftStateManager#updateWithMessage(Message) # update the aircraft state manager with the received messages in the AnimationTimer.
     * @see AircraftTableController#setOnDoubleClick(Consumer) # double-click behaviour to center on aircraft
     * @param stage the main window of the application.
     */
    @Override
    public void start(Stage stage) {
        // set up the behaviour upon double-click on the table
        table.setOnDoubleClick((state) -> map.centerOn(state.positionProperty().getValue()));
        // set up the whole Scene
        Scene scene = mainScene();
        // Show the Scene (window)
        Platform.runLater(() -> {
                    stage.setScene(scene);
                    stage.setTitle("Javions");
                    stage.setWidth(1920);
                    stage.setHeight(1080);
                    stage.show();
                });

        // Start the thread responsible for getting the messages and putting them in the queue
        Thread messageDecoding = new Thread(this::realTimeMessageDecoder);
        messageDecoding.setDaemon(true); // This thread will not prevent the JVM from exiting (if we close the window)
        messageDecoding.start(); // start to fill the queue

        // Now read the messages from the queue and update the aircraft states
        AnimationTimer messageProcessing = new javafx.animation.AnimationTimer() {
            private long lastPurge = Instant.now().getEpochSecond();
            @Override
            public void handle(long now) {
                Message m;
                while ((m = messageQueue.poll()) != null) {
                    aircraftStateManager.updateWithMessage(m);
                    messageCount.set(messageCount.get() + 1);
                }
                long currentSecond = Instant.now().getEpochSecond();
                // Purge the aircraft state manager every second
                if (currentSecond - lastPurge >= 1) { // Check if a second has passed since the last purge
                    aircraftStateManager.purge();
                    lastPurge = currentSecond;
                }
            }
        };
        messageProcessing.start(); // get the messages from the queue and update the aircraft states
    }

    /**
     * This method reads the messages from the binary file and puts them in the queue.
     * It also sleeps to simulate real time.
     */
    private void realTimeMessageDecoder() {
        long lastMessageTimeStampNs = 0; // the time stamp of the last message
        long lastTime = 0; // the time stamp of the system at the last message decoding
        byte[] bytes = new byte[RawMessage.LENGTH];
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            while (s.available() >= RawMessage.LENGTH) {
                long timeStampNs = s.readLong();
                // Sleep to simulate real time
                lastTime = sleepIfNecessary(timeStampNs, lastMessageTimeStampNs, lastTime);
                lastMessageTimeStampNs = timeStampNs;
                // End of sleep to simulate real time
                s.readNBytes(bytes, 0, bytes.length);
                Message m = MessageParser.parse(new RawMessage(timeStampNs, new ByteString(bytes)));
                if (Objects.nonNull(m)) {
                    messageQueue.add(m); // adds the message to the end of the queue
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method sleeps if necessary to simulate real time delays of messages and returns the current time.
     *
     * @param timeStampNs            : the time stamp of the message
     * @param lastMessageTimeStampNs : the time stamp of the last message
     * @param lastTime               : the last time the system handled a message
     * @return : the current time of the system
     */
    private long sleepIfNecessary(long timeStampNs, long lastMessageTimeStampNs, long lastTime) {
        long currentTime = System.nanoTime();
        long messageTimeDifference = TimeUnit.NANOSECONDS.toMillis(timeStampNs - lastMessageTimeStampNs);
        long programTimeDifference = TimeUnit.NANOSECONDS.toMillis(currentTime - lastTime);
        if (messageTimeDifference > programTimeDifference) {
            try {
                Thread.sleep(messageTimeDifference - programTimeDifference);
            } catch (InterruptedException e) {
                // Restore the interrupted status
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        return System.nanoTime(); // returns the current time of the system after the sleep and not currentTime (before the sleep)
    }

    /**
     * This method creates the scene graph corresponding to the graphical interface.
     *
     * @see #start(Stage)  the start method of the program.
     * @return the scene graph corresponding to the graphical interface.
     */
    private Scene mainScene() {
        // Bind the number of visible aircraft to the size of the aircraft state manager's set
        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));
        statusLineController.messageCountProperty().bind(messageCount);

        Pane mapWithPlanes = new StackPane(map.getPane(), mapOverlay.getPane());
        BorderPane aircraftTablePane = new BorderPane();
        // Configure the aircraft table pane
        aircraftTablePane.setTop(statusLineController.getPane());
        aircraftTablePane.setCenter(table.getPane());

        SplitPane root = new SplitPane();
        // Configure the root layout
        root.setOrientation(javafx.geometry.Orientation.VERTICAL);
        root.getItems().addAll(mapWithPlanes, aircraftTablePane);

        return new Scene(root);
    }
}
