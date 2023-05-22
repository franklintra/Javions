package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * @author @chukla (357550)
 * @project Javions
 */

public final class AircraftController {
    //todo: comment this class !! @chukla
    private final static double maxAltitude = 12000;
    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final Pane pane;
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

    public Pane getPane() {
        return pane;
    }

    private void createPane() {
        // Add observer to the set of observable aircraft states
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                createGroup(change.getElementAdded());
            } else if (change.wasRemoved()) {
                removeGroup(change.getElementRemoved().getIcaoAddress().string());
            }
        });
    }


    private void createGroup(ObservableAircraftState state) {
        Group group = new Group();
        pane.getChildren().add(group);
        group.setId(state.getIcaoAddress().string());

        // set the view order property to the negation of the altitude
        group.viewOrderProperty().bind(state.altitudeProperty().negate());

        // label and icon group creation and add it to the aircraft group
        Group labelIcon = new Group();
        group.getChildren().add(labelIcon);

        // icon creation and add it to the label and icon group
        labelIcon.getChildren().add(constructIcon(state));

        // construct trajectory
        constructTrajectory(state, group);

        // construct label
        constructLabel(state, labelIcon);

        // position the icon/label group
        positionLabelIcon(state, labelIcon);
    }


    private void positionLabelIcon(ObservableAircraftState state, Group labelIcon) {
        labelIcon.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
            return screenCoordinates.getX();
        }, mapParameters.zoomLevelProperty(), state.positionProperty(), mapParameters.minXProperty()));

        labelIcon.layoutYProperty().bind(Bindings.createDoubleBinding(() -> {
            Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
            return screenCoordinates.getY();
        }, mapParameters.zoomLevelProperty(), state.positionProperty(), mapParameters.minYProperty()));
    }


    private SVGPath constructIcon(ObservableAircraftState state) {
        SVGPath iconSVG = new SVGPath();
        // associate style class with icon node
        iconSVG.getStyleClass().add("aircraft");

        AircraftIcon icon = AircraftIcon.iconFor(state.aircraftData().typeDesignator(), state.aircraftData().description(), state.getCategory(), state.aircraftData().wakeTurbulenceCategory());
        // set the icon's path to the icon's SVG path
        iconSVG.setContent(icon.svgPath());

        // set rotation angle if the icon can rotate
        iconSVG.rotateProperty().bind(Bindings.createDoubleBinding(() -> {
            if (icon.canRotate()) {
                return Units.convertTo(state.trackOrHeadingProperty().getValue(), Units.Angle.DEGREE);
            } else {
                state.setTrackOrHeading(0);
                return 0.0;
            }
        }, state.trackOrHeadingProperty()));

        // set fill color based on altitude
        iconSVG.fillProperty().bind(state.altitudeProperty().map(alt -> ColorRamp.PLASMA.at((Math.pow((double) alt / maxAltitude, lowAltDefiner)))));

        // action when clicked on icon
        iconSVG.setOnMouseClicked(event -> selectedAircraft.set(state));

        return iconSVG;
    }


    private void constructLabel(ObservableAircraftState state, Group labelIcon) {
        // create the label and add it to the label and icon group
        Group label = new Group();
        // associate style class with label node
        label.getStyleClass().add("label");
        labelIcon.getChildren().add(label);

        drawLabel(state, label);
        //property visible must be bound to an expression that is only true when the zoom level is greater than or equal to 11 or selectedAircraft is one to which the label corresponds
        label.visibleProperty().bind(selectedAircraft.isEqualTo(state).or(mapParameters.zoomLevelProperty().greaterThanOrEqualTo(11)));
    }

    private void drawLabel(ObservableAircraftState state, Group label) {
        // create and add background and text to the group of label
        Rectangle background = new Rectangle();
        Text text = new Text();
        label.getChildren().add(background);
        label.getChildren().add(text);

        // drawing of the label i.e. background and text, also ensures that the text of altitude always is in metres and velocity is in km/h
        text.textProperty().bind(Bindings.createStringBinding(() -> {
            String velocity = Double.compare(state.getVelocity(), Double.NaN) == 0 ? "? km/h" : String.format("%.0f km/h", Units.convertTo(state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR));
            String altitude = Double.compare(state.getAltitude(), Double.NaN) == 0 ? "? m" : String.format("%.0f m", Units.convertTo(state.getAltitude(), Units.Length.METER));
            return labelFirstLine(state) + "\n" + velocity + "\u2002" + altitude;
        }, state.velocityProperty(), state.altitudeProperty()));

        // height and width should be bound to an expression whose value is equal to the height/width of the text of the label, plus 4.
        background.heightProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 4));
        background.widthProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));
    }


    private String labelFirstLine(ObservableAircraftState state) {
        if (state.aircraftData().registration() != null) {
            return state.aircraftData().registration().string();
        } else if (state.getCallSign() != null) {
            return state.getCallSign().string();
        } else {
            return state.getIcaoAddress().string();
        }
    }


    private void removeGroup(String groupID) {
        //remove the group from the pane
        pane.getChildren().removeIf(group -> group.getId().equals(groupID));

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

    private void constructTrajectory(ObservableAircraftState state, Group group) {
        Group trajectory = new Group();
        // associate style class with trajectory node
        trajectory.getStyleClass().add("trajectory");
        group.getChildren().add(trajectory);
        trajectory.visibleProperty().bind(selectedAircraft.isEqualTo(state));

        trajectory.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectory.layoutYProperty().bind(mapParameters.minYProperty().negate());

        trajectory.visibleProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue) {
                mapParameters.zoomLevelProperty().addListener((observable1, oldValue1, newValue1) -> drawTrajectory(state, trajectory));
                state.getTrajectory().addListener((ListChangeListener<ObservableAircraftState.AirbornePos>) c -> drawTrajectory(state, trajectory));
            } else {
                trajectory.getChildren().clear();
            }
        });
    }

    private void drawTrajectory(ObservableAircraftState state, Group trajectory) {
        trajectory.getChildren().clear();

        List<ObservableAircraftState.AirbornePos> positions = state.getTrajectory();
        if (positions == null || positions.isEmpty()) {
            return;
        }

        double x = WebMercator.x(mapParameters.getZoomLevel(), positions.get(0).pos().longitude());
        double y = WebMercator.y(mapParameters.getZoomLevel(), positions.get(0).pos().latitude());
        for (int i = 0; i < positions.size() - 1; i++) {
            Line line = new Line();  // Create a new Line object for each line segment
            double endX = WebMercator.x(mapParameters.getZoomLevel(), positions.get(i + 1).pos().longitude());
            double endY = WebMercator.y(mapParameters.getZoomLevel(), positions.get(i + 1).pos().latitude());
            line.setStartX(x);
            line.setStartY(y);
            line.setEndX(endX);
            line.setEndY(endY);
            trajectory.getChildren().add(line);

            x = endX;
            y = endY;

            double p1 = positions.get(i).altitude();
            double p2 = positions.get(i + 1).altitude();

            Color c1 = ColorRamp.PLASMA.at((Math.pow(p1 / maxAltitude, lowAltDefiner)));
            if (p1 != p2) {
                Color c2 = ColorRamp.PLASMA.at((Math.pow(p2 / maxAltitude, lowAltDefiner)));
                Stop s1 = new Stop(0, c1);
                Stop s2 = new Stop(1, c2);
                LinearGradient lg = new LinearGradient(x, y, endX, endY, true, NO_CYCLE, s1, s2);
                line.setStroke(lg);
            } else {
                line.setStroke(c1);
            }
        }
    }
}