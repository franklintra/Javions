package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;

import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class AircraftTableControllerTest extends Application {
    private static final GeoPos maison = new GeoPos((int) Units.convert(2.2794736259693136, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(48.88790023468289, Units.Angle.DEGREE, Units.Angle.T32));
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        AircraftStateManager aircraftStateManager = new AircraftStateManager(new AircraftDatabase("aircraft.zip"));
        ObservableSet<ObservableAircraftState> observableAircraftStates = aircraftStateManager.states();
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();

        var root = new BorderPane(new AircraftTableController(observableAircraftStates, selectedAircraftState).getPane());
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TestBaseMapController");
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.show();
    }
}
