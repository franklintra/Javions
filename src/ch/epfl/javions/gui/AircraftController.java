package ch.epfl.javions.gui;

import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.List;

/**
 * @author @chukla (357550)
 * @project Javions
 */

public final class AircraftController {

    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final Pane pane;
    private final static double maxAltitude = 12000;
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
                removeGroup(change.getElementRemoved());
            }
        });
    }



    private void createGroup(ObservableAircraftState state) {
        // create the group
        Group group = new Group();
        pane.getChildren().add(group);
        group.setId(state.getIcaoAddress().string());

        // set the view order property to the negation of the altitude
        group.viewOrderProperty().bind(state.altitudeProperty().negate());

        // label and icon group creation and add it to the aircraft group
        Group labelIcon = new Group();
        group.getChildren().add(labelIcon);
        // position the icon/label group
        positionLabelIcon(state, labelIcon);

        // icon creation and add it to the label and icon group
        labelIcon.getChildren().add(constructIcon(state));

        // construct trajectory
        constructTrajectory(state, group);

        // construct label
        constructLabel(state,labelIcon);
    }





    private void positionLabelIcon(ObservableAircraftState state, Group labelIcon) {
        // position the icon/label group
        labelIcon.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
            return screenCoordinates.getX();
        }, mapParameters.zoomLevelProperty()));

        labelIcon.layoutYProperty().bind(Bindings.createDoubleBinding(() -> {
            Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
            return screenCoordinates.getY();
        }, mapParameters.zoomLevelProperty()));
    }


    private SVGPath constructIcon(ObservableAircraftState state) {
        SVGPath iconSVG = new SVGPath();
        AircraftIcon icon = AircraftIcon.iconFor(state.aircraftData().typeDesignator(), state.aircraftData().description(), state.getCategory(), state.aircraftData().wakeTurbulenceCategory());
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



    private void constructLabel(ObservableAircraftState state, Group labelIcon) {
        // TODO: 5/12/2023 need a semi transparent background for the label
        // create the label and add it to the label and icon group
        Group label = new Group();
        // associate style class with label node
        label.getStyleClass().add("label");
        labelIcon.getChildren().add(label);

        // create and add background and text to the group of label
        Rectangle background = new Rectangle();
        Text text = new Text();
        label.getChildren().add(background);
        label.getChildren().add(text);

        // drawing of the label i.e. background and text
        // TODO: 5/11/2023 check if correct
        text.textProperty().bind(Bindings.createStringBinding(() -> {
            String velocity = Double.compare(state.getVelocity(), Double.NaN) == 0 ? "? km/h" : String.format("%f km/h", state.getVelocity());
            String altitude = Double.compare(state.getAltitude(), Double.NaN) == 0 ? "? meters" : String.format("%f meters", state.getAltitude());
            return labelFirstLine(state) + "\n" + velocity + "\u2002" + altitude;
        }, state.velocityProperty(), state.altitudeProperty()));

        // height and width should be bound to an expression whose value is equal to the height/width of the text of the label, plus 4.
        background.heightProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 4));
        background.widthProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));

        // ensures text of altitude always is in metres and velocity is in km/h
        text.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("%f meters %f km/h",
                                state.altitudeProperty().get(),
                                state.velocityProperty().get()),
                        state.altitudeProperty(), state.velocityProperty()
                )
        );

        //property visible must be bound to an expression that is only true when the zoom level is greater than or equal to 11 or selectedAircraft is one to which the label corresponds
        label.visibleProperty().bind(Bindings.createBooleanBinding(() -> mapParameters.getZoomLevel() >= 11 || selectedAircraft.get().equals(state), mapParameters.zoomLevelProperty(), selectedAircraft));
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

    private void constructTrajectory(ObservableAircraftState state, Group group) {

        Group trajectory = new Group();
        // associate style class with trajectory node
        trajectory.getStyleClass().add("trajectory");
        group.getChildren().add(trajectory);
        trajectory.visibleProperty().bind(selectedAircraft.isEqualTo(state));

        trajectory.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                drawTrajectory(state, trajectory);
            } else {
                trajectory.getChildren().clear();
            }
        });

        mapParameters.zoomLevelProperty().addListener((observable, oldValue, newValue) -> {
            if (trajectory.isVisible()) {
                drawTrajectory(state, trajectory);
            }
        });

        state.observableTrajectoryProperty().addListener((observable, oldValue, newValue) -> {
            if (trajectory.isVisible()) {
                    drawTrajectory(state, trajectory);
            }
        });

        trajectory.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectory.layoutYProperty().bind(mapParameters.minXProperty().negate());
    }

    private void drawTrajectory(ObservableAircraftState state, Group trajectory) {

        List<ObservableAircraftState.AirbornePos> positions = state.getObservableTrajectory();
        if (positions == null || positions.isEmpty()) {
            return;
        }

        Line line = new Line();
        double x = WebMercator.x(mapParameters.getZoomLevel(),positions.get(0).pos().longitude());
        double y = WebMercator.y(mapParameters.getZoomLevel(),positions.get(0).pos().latitude());
        line.setStartX(x);
        line.setStartY(y);

        for (int i = 1; i < positions.size() -1; i++) {
            double endX = WebMercator.x(mapParameters.getZoomLevel(),positions.get(i).pos().longitude());
            double endY = WebMercator.y(mapParameters.getZoomLevel(),positions.get(i).pos().latitude());
            line.setEndX(endX);
            line.setEndY(endY);
            trajectory.getChildren().add(line);

            line = new Line();
            line.setStartX(x);
            line.setStartY(y);


            double p1 = positions.get(i - 1).altitude();
            double p2 = positions.get(i).altitude();

            Color c1 = ColorRamp.PLASMA.at(p1);
            if (p1 == p2) {
                line.setStroke(c1);
            } else {
                Color c2 = ColorRamp.PLASMA.at(p2);
                Stop s1 = new Stop(0, c1);
                Stop s2 = new Stop(1, c2);
                LinearGradient lg = new LinearGradient(x, y, endX, endY, false, CycleMethod.NO_CYCLE, s1, s2);
                line.setStroke(lg);
            }
        }

        double lastX = WebMercator.x(mapParameters.getZoomLevel(),positions.get(positions.size() - 1).pos().longitude());
        double lastY = WebMercator.y(mapParameters.getZoomLevel(),positions.get(positions.size() - 1).pos().latitude());
        line.setEndX(lastX);
        line.setEndY(lastY);
        trajectory.getChildren().add(line);
    }
}