package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
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
    // Configuration variables. All are public static and final, so they can be accessed from anywhere in the program.
    public static final Path tileCache = Path.of("tile-cache");
    public static final String tileServerUrl = "tile.openstreetmap.org";
    public static final String aircraftDatabasePath = Objects.requireNonNull(Main.class.getResource("/aircraft.zip")).getPath();
    public static final int defaultZoomLevel = 8;
    public static final int defaultX = 33530;
    public static final int defaultY = 23070;
    public static final int defaultWidth = 1920;
    public static final int defaultHeight = 1080;
    public static RunningMode runningMode;
    public static String simulationPath;
    /**
     * The messageQueue field is a concurrent linked queue that contains the messages
     * sampled and decoded in real time from the input stream (messages.bin or radio)
     */
    private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    // End of configuration

    /**
     * The main method of the program.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            runningMode = RunningMode.SIMULATION;
            simulationPath = args[0];
            System.out.println("Running in " + runningMode + " mode with file " + simulationPath);
        } else {
            runningMode = RunningMode.RADIO;
            System.out.println("Running in " + runningMode + " mode with System.in");
        }
        launch(args);
    }

    /**
     * The start method creates all necessary controllers,
     * starts the application by showing the main gui,
     * starting the thread responsible for getting the messages,
     * and finally overriding part of the "animation timer" responsible for updating the aircraft states
     * based on the received messages. It purges the aircraft state manager every second.
     * It also adds behaviour on double-click on the table to center the map on the selected aircraft.
     *
     * @param stage the main window of the application.
     * @see #mainScene(BaseMapController, AircraftController, AircraftTableController, StatusLineController, IntegerProperty, LongProperty)
     * the construction of the main #Scene object.
     * @see AnimationTimer the animation timer responsible for updating the aircraft states based on the received messages.
     * @see AircraftStateManager#purge() # purge the aircraft state manager every second in the AnimationTimer.
     * @see AircraftStateManager#updateWithMessage(Message) # update the aircraft state manager with the received messages in the AnimationTimer.
     * @see AircraftTableController#setOnDoubleClick(Consumer) # double-click behaviour to center on aircraft
     */
    @Override
    public void start(Stage stage) {
        // Start the thread responsible for getting the messages and putting them in the queue
        // This thread is started before the controllers are created and the gui is shown,
        // so that the queue is already filled when the GUI is shown
        configureAndStartMessageDecodingThread();

        /*
         * This field contains access to the database of all aircraft.
         * It is polled to get additional aircraft's information.
         */
        AircraftDatabase database = new AircraftDatabase(aircraftDatabasePath);

        /*
         * The manager of the aircraft states. It gives access to currently visible aircraft states.
         * Also sets up a property corresponding to the number of visible aircraft.
         */
        AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
        IntegerProperty numberOfVisibleAircraft = new SimpleIntegerProperty(0);
        numberOfVisibleAircraft.bind(Bindings.size(aircraftStateManager.states()));
        /*
         * The selectedAircraftState property is an object property that contains the currently selected aircraft state.
         * It is used to sync the aircraft state selected in the table and the map.
         */
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();
        /*
         * The tileManager field is a TileManager that manages the tiles of the map. It is used to create the BaseMapController.
         * @see BaseMapController
         */
        TileManager tileManager = new TileManager(tileCache, tileServerUrl);
        /*
         * The mapParameters field is a MapParameters that defines the parameters of the map. It is used to create the BaseMapController.
         * It contains zoom level and allows for scrolling.
         * Its default value is zoom level 8, with the top left corner of the map at the coordinates (33530, 23070).
         * @see BaseMapController
         */
        MapParameters mapParameters = new MapParameters(defaultZoomLevel, defaultX, defaultY);
        /*
         * The map field is the controller of the map. It is used to create the map pane.
         */
        BaseMapController map = new BaseMapController(tileManager, mapParameters);
        /*
         * The ac field is the controller of the overlay of the map.
         * It is used to draw trajectories, aircraft and labels.
         */
        AircraftController mapOverlay = new AircraftController(mapParameters, aircraftStateManager.states(), selectedAircraftState);
        /*
         * The table field is the controller of the aircraft table. It is used to create the aircraft table pane.
         * Also sets up the behaviour upon double-click on the table
         */
        AircraftTableController table = new AircraftTableController(aircraftStateManager.states(), selectedAircraftState);
        table.setOnDoubleClick((state) -> map.centerOn(state.positionProperty().getValue()));
        /*
         * The statusLineController field is the controller of the status line (middle of the window).
         * It shows the number of visible aircraft and the number of messages received.
         */
        StatusLineController statusLineController = new StatusLineController();

        // Creates the property for the number of received message to show in the status line
        LongProperty messageCount = new SimpleLongProperty(0);

        // Sets up and shows the main scene
        Platform.runLater(() -> {
            Scene scene = mainScene(map, mapOverlay, table, statusLineController, numberOfVisibleAircraft, messageCount);
            stage.setScene(scene);
            stage.setTitle("Javions");
            stage.setWidth(defaultWidth);
            stage.setHeight(defaultHeight);
            stage.show();
        });

        // Now read the messages from the queue and update the aircraft states
        AnimationTimer messageProcessing = new javafx.animation.AnimationTimer() {
            private long lastPurge = 0;

            @Override
            public void handle(long now) {
                Message m;
                while ((m = messageQueue.poll()) != null) {
                    try {
                        aircraftStateManager.updateWithMessage(m);
                    } catch (IOException e) {
                        // this exception is thrown this way because the AnimationTimer does not allow checked exceptions
                        throw new UncheckedIOException(e);
                    }
                    messageCount.set(messageCount.get() + 1);
                }
                // Purge the aircraft state manager every second
                if (now - lastPurge >= 1e9) { // Check if a second has passed since the last purge (1e9 nanoseconds = 1 second)
                    aircraftStateManager.purge();
                    lastPurge = now;
                }
            }
        };
        messageProcessing.start(); // get the messages from the queue and update the aircraft states
    }

    /**
     * This method creates the scene graph corresponding to the graphical interface.
     *
     * @return the scene graph corresponding to the graphical interface.
     * @see #start(Stage)  the start method of the program.
     */
    private Scene mainScene(BaseMapController map, AircraftController mapOverlay, AircraftTableController table, StatusLineController statusLineController, IntegerProperty numberOfVisibleAircraft, LongProperty messageCount) {
        // Bind the number of visible aircraft to the size of the aircraft state manager's set
        statusLineController.aircraftCountProperty().bind(numberOfVisibleAircraft);
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

    /**
     * This method starts the thread that reads the messages from either the radio or the samples file.
     * It also sets it as a daemon thread so that it does not prevent the JVM from exiting when closing the GUI.
     *
     * @see #messagesDecoder()
     */
    private void configureAndStartMessageDecodingThread() {
        Thread messageDecoding;
        if (runningMode == RunningMode.SIMULATION) {
            messageDecoding = new Thread(this::messagesDecoder);
        } else if (runningMode == RunningMode.RADIO) {
            messageDecoding = new Thread(this::radioSamplesDecoder);
        } else {
            throw new IllegalStateException("The running mode is not valid");
        }
        messageDecoding.setDaemon(true); // This thread will not prevent the JVM from exiting (if we close the window)
        messageDecoding.start(); // start the thread (to fill the queue)
    }

    /**
     * This method reads the messages from the binary file and puts them in the queue.
     * It also sleeps to simulate real time.
     */
    private void messagesDecoder() {
        long lastMessageTimeStampNs = 0; // the time stamp of the last message
        long lastTime = 0; // the time stamp of the system at the last message decoding
        byte[] bytes = new byte[RawMessage.LENGTH];
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(new FileInputStream(simulationPath)))) {
            while (s.available() >= RawMessage.LENGTH) {
                long timeStampNs = s.readLong();
                // Sleep to simulate real time
                lastTime = sleepIfNecessary(timeStampNs, lastMessageTimeStampNs, lastTime);
                lastMessageTimeStampNs = timeStampNs;
                // End of sleep to simulate real time
                s.readNBytes(bytes, 0, bytes.length);
                RawMessage message = new RawMessage(timeStampNs, new ByteString(bytes));
                convertAndAddToQueue(message);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method reads the messages from the System.In and puts them in the queue.
     * It doesn't need to sleep to simulate real time because the messages are downloaded in real time.
     */
    private void radioSamplesDecoder() {
        try {
            AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
            RawMessage message;
            while (true) {
                if ((message = demodulator.nextMessage()) != null) {
                    convertAndAddToQueue(message);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Extracted method to convert a raw message to a message and add it to the queue
     * from both radioSamplesDecoder and messagesDecoder.
     *
     * @param message : the raw message to convert and add to the queue if it is not null
     */
    private void convertAndAddToQueue(RawMessage message) {
        Message m = MessageParser.parse(message);
        if (Objects.nonNull(m)) {
            messageQueue.add(m); // adds the message to the end of the queue
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

    public enum RunningMode {RADIO, SIMULATION}
}
