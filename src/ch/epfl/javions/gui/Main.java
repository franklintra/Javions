package ch.epfl.javions.gui;

import ch.epfl.CONFIGURATION;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;


/**
 * @author @franklintra (362694)
 * @project Javions
 * The Main class contains the main program. It extends Application, and has a main method
 * that does nothing but call launch.
 */
public final class Main extends Application {
    private final SimpleLongProperty messageCount = new SimpleLongProperty(0);
    private static final AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase(Objects.requireNonNull(Main.class.getResource("/aircraft.zip")).getPath()));
    private static final ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();
    private static final Path tileCache = Path.of("tile-cache");
    private static final TileManager tm = new TileManager(tileCache, CONFIGURATION.MAP.TILE_SERVER_URL);
    private static final MapParameters mp = new MapParameters(17, 17_389_327, 11_867_430);
    private static final AircraftTableController table = new AircraftTableController(aircraftStateManager.states(), selectedAircraftState);
    private static final BaseMapController map = new BaseMapController(tm, mp);
    private static final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    private final ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
    private final AircraftController ac = new AircraftController(mp, aircraftStateManager.states(), sap);
    /**
     * The main method of the program.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method starts the application by constructing the scene graph corresponding
     * to the graphical interface, starting the thread responsible for getting the messages,
     * and finally starting the "animation timer" responsible for updating the aircraft states
     * based on the received messages.
     *
     * @param primaryStage the main window of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        StatusLineController statusLineController = new StatusLineController();
        SplitPane root = new SplitPane();
        Pane mapWithPlanes = new StackPane(map.getPane(), ac.getPane());
        BorderPane aircraftTablePane = new BorderPane(table.getPane());
        // Configure the root layout
        root.setOrientation(javafx.geometry.Orientation.VERTICAL);
        root.getItems().addAll(mapWithPlanes, aircraftTablePane);

        // Configure the aircraft table pane
        aircraftTablePane.setTop(statusLineController.getPane());

        // Bind the number of visible aircraft to the size of the aircraft state manager's set
        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));
        statusLineController.messageCountProperty().bind(messageCount);

        // Set up the scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Javions");
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setMinWidth(1920);
        primaryStage.setMinHeight(1080);
        primaryStage.show();

        // Start the thread responsible for getting the messages and putting them in the queue
        Thread messageDecoding = new Thread(this::realTimeMessageDecoder);
        messageDecoding.setDaemon(true); // This thread will not prevent the JVM from exiting (if we close the window)
        messageDecoding.start(); // start to fill the queue

        // Now read the messages from the queue and update the aircraft states
        AnimationTimer messageProcessing = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                Message m;
                while ((m = messageQueue.poll()) != null) {
                    try {
                        aircraftStateManager.updateWithMessage(m);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    messageCount.set(messageCount.get() + 1);
                }
            }
        };
        messageProcessing.start(); // get the messages from the queue and update the aircraft states todo: check if i can do it this way instead of AnimationTimer

        // Purge the aircraft state manager every second
        // (this is done in a separate thread and won't block the UI nor block the JVM from exiting)
        newSingleThreadScheduledExecutor().scheduleAtFixedRate(aircraftStateManager::purge, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * This method reads the messages from the binary file and puts them in the queue.
     * It also sleeps to simulate real time.
     */
    private void realTimeMessageDecoder() {
        long lastMessageTimeStampNs = 0; // the time stamp of the last message
        long lastTime = 0; // the time stamp of the system at the last message decoding
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/messages_20230318_0915.bin"))))) {
            while (s.available() >= RawMessage.LENGTH) {
                long timeStampNs = s.readLong();
                // Sleep to simulate real time
                lastTime = sleepIfNecessary(timeStampNs, lastMessageTimeStampNs, lastTime);
                lastMessageTimeStampNs = timeStampNs;
                // End of sleep to simulate real time
                Message m = readAndParseMessage(s, timeStampNs);
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
     * @param timeStampNs : the time stamp of the message
     * @param lastMessageTimeStampNs : the time stamp of the last message
     * @param lastTime : the last time the system handled a message
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
     * This method reads the message from the input stream and parses it.
     * It is extracted code from the realTimeMessageDecoder method for better readability.
     * @param s : the input stream
     * @param timeStampNs : the time stamp of the message
     * @return : the parsed message
     * @throws IOException : if the input stream is not valid
     */
    private Message readAndParseMessage(DataInputStream s, long timeStampNs) throws IOException {
        byte[] bytes = new byte[RawMessage.LENGTH];
        s.readNBytes(bytes, 0, bytes.length);
        return MessageParser.parse(new RawMessage(timeStampNs, new ByteString(bytes)));
    }
}
