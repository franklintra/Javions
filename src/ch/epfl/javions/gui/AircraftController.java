package ch.epfl.javions.gui;

import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;

/**
 * @author @chukla (357550)
 * @project Javions
 */


public final class AircraftController {

    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final Pane pane;
    private final double maxAltitude = 12000;
    private final double lowAltDefiner = (double) 1 / 3;

    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.selectedAircraft = selectedAircraft;
        this.states = states;
        this.mapParameters = mapParameters;
        pane = new Pane();
        pane.getStylesheets().add("aircraft.css");
        pane.setPickOnBounds(false); // allows map to receive mouse events when user clicks on transparent part of aircraft

        createPane();
    }

    public Pane pane() {
        return pane;
    }

    private void createPane() {
        // Add observer to the set of observable aircraft states
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                createGroup(change.getElementAdded());
            } else if (change.wasRemoved()) {
                removeGroup(change.getElementRemoved());
            }
        });
    }

    private void createGroup(ObservableAircraftState state) {
        // create the group
        Group group = new Group();
        pane.getChildren().add(group);
        group.setId(state.getIcaoAddress().string()); // TODO: 5/11/2023 check if correct

        // set the view order property to the negation of the altitude
        group.viewOrderProperty().bind(state.altitudeProperty().negate());

        // trajectory group creation and add it to the aircraft group
        Group trajectory = new Group();
        group.getChildren().add(trajectory);
        trajectory.getChildren().add(createTrajectory(state));

        // associate style class with trajectory node
        trajectory.getStyleClass().add("trajectory");

        // label and icon group creation and add it to the aircraft group
        Group labelIcon = new Group();
        group.getChildren().add(labelIcon);

        // position the icon/label group
        // TODO: 5/9/2023 figure out how to use layoutXProperty() and layoutYProperty() or to use it in getScreenCoordinates()
        Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
        labelIcon.layoutXProperty().bind(Bindings.createDoubleBinding(screenCoordinates::getX, mapParameters.zoomLevelProperty()));
        labelIcon.layoutYProperty().bind(Bindings.createDoubleBinding(screenCoordinates::getY, mapParameters.zoomLevelProperty()));


        // icon creation and add it to the label and icon group
        labelIcon.getChildren().add(constructIcon(state));

        // create the label and add it to the label and icon group
        Group label = new Group();
        labelIcon.getChildren().add(label);

        // associate style class with label node
        label.getStyleClass().add("label");

        // create and add background and text to the group of label
        Rectangle background = new Rectangle();
        Text text = new Text();
        constructLabel(state, background, text);
        label.getChildren().add(background);
        label.getChildren().add(text);
    }

    private SVGPath constructIcon(ObservableAircraftState state) {
        SVGPath iconSVG = new SVGPath();
        AircraftIcon icon = AircraftIcon.iconFor(state.getAircraftData().typeDesignator(), state.getAircraftData().description(), state.getCategory(), state.getAircraftData().wakeTurbulenceCategory());
        // set the icon's path to the icon's SVG path
        iconSVG.setContent(icon.svgPath());

        // set rotation angle if the icon can rotate
        if (icon.canRotate()) {
            iconSVG.rotateProperty().bind(state.trackOrHeadingProperty());
        } else {
            iconSVG.rotateProperty().setValue(0);
        }

        // set fill color based on altitude
        iconSVG.fillProperty().bind(state.altitudeProperty().map(alt -> ColorRamp.PLASMA.at((Math.pow((double) alt / maxAltitude, lowAltDefiner)))));
        // associate style class with icon node
        iconSVG.getStyleClass().add("aircraft");

        // action when clicked on icon
        iconSVG.setOnMouseClicked(event -> selectedAircraft.set(state));

        return iconSVG;
    }

    private void constructLabel(ObservableAircraftState state, Rectangle background, Text text) {

        // height and width should be bound to an expression whose value is equal to the height/width of the text of the label, plus 4.
        background.heightProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 4));
        background.widthProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));

        // ensures text of altitude always is in metres and velocity is in km/h
        // TODO: 5/11/2023 how to do format for two things
        text.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("%f meters", "%km/h meters",
                                state.altitudeProperty().get(),
                                state.velocityProperty().get(),
                        state.altitudeProperty(), state.velocityProperty()
                )
        ));

        //property visible must be bound to an expression that is only true when the zoom level is greater than or equal to 11 or selectedAircraft is one to which the label corresponds
        text.visibleProperty().bind(Bindings.createBooleanBinding(() -> mapParameters.getZoomLevel() >= 11 || selectedAircraft.get() == state, mapParameters.zoomLevelProperty(), selectedAircraft));

        // drawing of the label i.e. background and text
        // TODO: 5/11/2023 check if correct 
        String velocity = Double.compare(state.getVelocity(), Double.NaN) == 0 ? "? km/h" : String.format("%f km/h", state.getVelocity());
        String altitude = Double.compare(state.getAltitude(), Double.NaN) == 0 ? "? meters" : String.format("%f meters", state.getAltitude());

        text.textProperty().bind(Bindings.createStringBinding(() ->
                        labelFirstLine(state) + "\n" + velocity + "\u2002" + altitude,
                state.velocityProperty(), state.altitudeProperty()));
    }

    private String labelFirstLine(ObservableAircraftState state) {
        if (state.getRegistration() != null) {
            return state.getRegistration().string();
        } else if (state.getCallSign() != null) {
            return state.getCallSign().string();
        } else {
            return state.getIcaoAddress().string();
        }
    }

    private void removeGroup(ObservableAircraftState state) {
        //remove the group from the pane
        states.remove(state);
    }

    private Point2D getScreenCoordinates(ObservableAircraftState state, MapParameters mapParams) {
        int zoomLevel = mapParams.getZoomLevel();
        double minX = mapParams.getMinX();
        double minY = mapParams.getMinY();
        double longitude = state.getPosition().longitude();
        double latitude = state.getPosition().latitude();

        double x = (WebMercator.x(zoomLevel, longitude) - minX);
        double y = (WebMercator.y(zoomLevel, latitude) - minY);

        return new Point2D(x, y);
    }

    private SVGPath createTrajectory(ObservableAircraftState state) {
        SVGPath trajectory = new SVGPath();


        // trajectory has to be visible then the graphical representation of trajectory is recreated each time trajectory or zoom level changes
        // FIXME: 5/11/2023 fix this
        trajectory.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // add listeners to zoomLevelProperty() and trajectoryProperty()
                mapParameters.zoomLevelProperty().addListener((observable1, oldValue1, newValue1) -> {
                    trajectory.setContent(state.getTrajectory().toString());
                });
//                state.trajectoryProperty().addListener((observable2, oldValue2, newValue2) -> {
////                    trajectory.setContent(state.getTrajectory().toString());
////                });
            }
        });

        // visible property only true when its own state is contained in property passed to constructor
        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(() -> selectedAircraft.get() == state, selectedAircraft));


        // TODO: 5/9/2023 complete trajectory implementattion
        // TODO: 5/9/2023 implement colouring of trajectory
        return trajectory;
    }
}