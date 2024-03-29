package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.*;
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
import java.util.*;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * @author @chukla (357550)
 * @project Javions
 * The AircraftController class
 * This class is responsible for the creation of the aircraft on the map and their respective icons, trajectories, and labels.
 * It also handles the necessary actions when an aircraft is selected and the movement/rotation of the aircraft.
 */
public final class AircraftController {
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final Pane pane;
    private final MapParameters mapParameters;
    ApiController apiController = ApiController.getInstance();
    private final static double maxAltitude = 12000; // highest approximate altitude at which airliners fly
    private final double lowAltDefiner = (double) 1 / 3; // power that distinguishes more finely the low altitudes

    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.selectedAircraft = selectedAircraft;
        this.states = states;
        this.mapParameters = mapParameters;
        pane = new Pane();
        pane.getStylesheets().add("aircraft.css"); // add the css file to the pane
        pane.setPickOnBounds(false); // allows map to receive mouse events when user clicks on transparent part of aircraft
        createPane();

        selectedAircraft.addListener((observable, oldValue, newValue) -> {
            CallSign key = newValue.getCallSign();
            if (key != null) {
                apiController.memory.computeIfAbsent(key, ApiController::getData);
            }
        });
    }

    /**
     * @return the pane
     */
    public Pane getPane() {
        return pane;
    }

    /**
     * Adds an observer to the set of observable aircraft states.
     * This observer is responsible handling additions and removals of aircraft states in the set and creates/removes
     * the entire Aircraft GUI accordingly
     */
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

    /**
     * Creates a group representing an aircraft state and adds it to the pane.
     * This method constructs the label, icon, and trajectory for the aircraft state,
     * and positions the icon/label group accordingly.
     *
     * @param state The observable aircraft state for which to create the group.
     */
    private void createGroup(ObservableAircraftState state) {
        Group aircraftGUI = new Group();
        pane.getChildren().add(aircraftGUI);
        aircraftGUI.setId(state.getIcaoAddress().string()); // set the id of the aircraft group to the ICAO address
        // set the view order property to the negation of the altitude
        aircraftGUI.viewOrderProperty().bind(state.altitudeProperty().negate());
        // label and icon group creation and add it to the aircraft group
        Group labelIconGUI = new Group();
        aircraftGUI.getChildren().add(labelIconGUI);
        // icon creation and add it to the label and icon group
        labelIconGUI.getChildren().add(constructIcon(state));
        // construct label
        constructLabel(state, labelIconGUI);
        // position the icon/label group
        positionLabelIcon(state, labelIconGUI);
        // construct trajectory
        constructTrajectory(state, aircraftGUI);
    }

    /**
     * Constructs an SVG icon for the specified aircraft state.
     * Handles the rotation, colouring, and styling of the icon.
     *
     * @param state The observable aircraft state for which to construct the icon.
     * @return The constructed SVG icon as an SVGPath node.
     */
    private SVGPath constructIcon(ObservableAircraftState state) {
        SVGPath iconSVG = new SVGPath();
        iconSVG.getStyleClass().add("aircraft"); // associate style class with icon node
        AircraftIcon icon = Objects.isNull(state.aircraftData()) ?
                AircraftIcon.UNKNOWN :
                AircraftIcon.iconFor(state.aircraftData().typeDesignator(), state.aircraftData().description(), state.getCategory(), state.aircraftData().wakeTurbulenceCategory());
        iconSVG.setContent(icon.svgPath()); // set the icon's path to the icon's SVG path

        // rotate the icon
        rotateIcon(iconSVG, icon, state);

        // set fill color based on altitude
        iconSVG.fillProperty().bind(state.altitudeProperty().map(alt -> ColorRamp.PLASMA.at(Math.pow((double) alt / maxAltitude, lowAltDefiner))));
        // sets the current aircraft state as the selected aircraft when the icon is clicked
        iconSVG.setOnMouseClicked(event -> selectedAircraft.set(state));
        return iconSVG;
    }

    /**
     * Rotates the specified SVG icon based on the track or heading of the aircraft state.
     * If the icon supports rotation, the rotation is set to match the track or heading value.
     * If the icon does not support rotation, the rotation is set to 0 degrees.
     *
     * @param iconSVG the SVGPath object representing the icon to rotate
     * @param icon    the AircraftIcon object associated with the icon
     * @param state   the ObservableAircraftState object representing the state of the aircraft
     */
    private void rotateIcon(SVGPath iconSVG, AircraftIcon icon, ObservableAircraftState state) {
        // set rotation based on track or heading if the icon can rotate else don't rotate
        if (icon.canRotate()) {
            iconSVG.rotateProperty().bind(state.trackOrHeadingProperty().map((val) -> Units.convertTo((Double) val, Units.Angle.DEGREE)));
        } else {
            iconSVG.setRotate(0);
        }
    }

    /**
     * Constructs a label for the specified aircraft state and adds it to the label and icon group.
     *
     * @param state     The observable aircraft state for which to construct the label.
     * @param labelIcon The Group representing the label and icon group.
     */
    private void constructLabel(ObservableAircraftState state, Group labelIcon) {
        Group label = new Group();
        label.getStyleClass().add("label"); // associate style class with label node
        labelIcon.getChildren().add(label); // add label to label and icon group
        drawLabel(state, label);
        //property visible must be bound to an expression that is only true when the zoom level is greater than or equal to 11 or selectedAircraft is one to which the label corresponds
        label.visibleProperty().bind(selectedAircraft.isEqualTo(state).or(mapParameters.zoomLevelProperty().greaterThanOrEqualTo(11)));
    }

    /**
     * Draws a label for an aircraft state on the given group.
     *
     * @param state The observable aircraft state.
     * @param label The group to which the label will be added.
     */
    private void drawLabel(ObservableAircraftState state, Group label) {
        // create and add background and text to the group of label
        Rectangle background = new Rectangle();
        Text text = new Text();
        label.getChildren().add(background);
        label.getChildren().add(text);
        // drawing of the label i.e. background and text, also ensures that the text of altitude always is in metres and velocity is in km/h and converts the values to the correct units
        text.textProperty().bind(labelText(state));

        // height and width should be bound to an expression whose value is equal to the height/width of the text of the label, plus 4.
        background.heightProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 4));
        background.widthProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));
    }

    /**
     * Constructs the text of the label for the specified aircraft state.
     * It is a binding to the first and second line of the label.
     * It returns an observable string value that is the concatenation of the first and second line of the label.
     *
     * @param state The observable aircraft state for which to construct the text of the label.
     * @return The text of the label as an observable string value.
     * @see AircraftController#aircraftIdentificationLabelLine(ObservableAircraftState)
     * @see AircraftController#aircraftSpeedAndAltitudeLine(ObservableAircraftState)
     */
    private ObservableStringValue labelText(ObservableAircraftState state) {
        ObservableStringValue firstLine = aircraftIdentificationLabelLine(state);
        ObservableStringValue secondLine = aircraftSpeedAndAltitudeLine(state);
        ObservableStringValue nextLinesIfAvailable = apiDataLine(state);
        return Bindings.createStringBinding(() -> firstLine.get() + "\n" + secondLine.get() + nextLinesIfAvailable.get(), firstLine, secondLine, nextLinesIfAvailable);
    }

    /**
     * Constructs the first line of the label for the specified aircraft state.
     *
     * @param state The observable aircraft state for which to construct the first line of the label.
     *              If the aircraft data is not null, the first line of the label is the aircraft's registration
     *              and won't change hence no bindings.
     *              If the aircraft data is null, the first line of the label is the aircraft's call sign or ICAO address
     *              depending on whether the call sign is null or not. Hence, the first line of the label is bound to the call sign property.
     * @return The first line of the label as an observable string value.
     */
    private ObservableStringValue aircraftIdentificationLabelLine(ObservableAircraftState state) {
        StringProperty labelFirstLine = new SimpleStringProperty();
        if (Objects.nonNull(state.aircraftData())) {
            labelFirstLine.set(state.aircraftData().registration().string());
        } else {
            labelFirstLine.bind(Bindings.createStringBinding(() -> {
                if (Objects.nonNull(state.getCallSign())) {
                    return state.getCallSign().string();
                } else {
                    return state.getIcaoAddress().string();
                }
            }, state.callSignProperty()));
        }
        return labelFirstLine;
    }

    /**
     * Constructs the next lines of the label for the specified aircraft state.
     * @param state : the current aircraft state
     * @return an observable string containing the data from the api if loaded
     */
    private ObservableStringValue apiDataLine(ObservableAircraftState state) {
        return Bindings.createStringBinding(() -> {
            ApiController.ApiData data = apiController.memory.get(state.getCallSign());
            return data == null ? "" : "\n" + data;
        }, selectedAircraft, apiController.memory); // state is the observed value.
    }

    /**
     * Constructs the second line of the label for the specified aircraft state.
     * The second line of the label is the aircraft's velocity and altitude.
     * This value is an ObservableStringValue as it is bound to the velocity and altitude properties of the aircraft state.
     *
     * @param state The observable aircraft state for which to construct the second line of the label.
     * @return The second line of the label as an observable string value.
     */
    private ObservableStringValue aircraftSpeedAndAltitudeLine(ObservableAircraftState state) {
        StringProperty labelSecondLine = new SimpleStringProperty();
        labelSecondLine.bind(Bindings.createStringBinding(() -> {
            String velocity = Double.compare(state.getVelocity(), Double.NaN) == 0 ? "? km/h" : String.format("%.0f km/h", Units.convertTo(state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR));
            String altitude = Double.compare(state.getAltitude(), Double.NaN) == 0 ? "? m" : String.format("%.0f m", Units.convertTo(state.getAltitude(), Units.Length.METER));
            return velocity + "\u2002" + altitude;
        }, state.velocityProperty(), state.altitudeProperty()));
        return labelSecondLine;
    }

    /**
     * Positions the label and icon group according to the specified aircraft state.
     * The position of the group is bound to the screen coordinates calculated based on the state's position
     * and the map parameters, such as zoom level and minimum X/Y values.
     *
     * @param state     The observable aircraft state for which to position the label and icon group.
     * @param labelIcon The group representing the label and icon.
     */
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

    /**
     * Constructs and associates a trajectory with a group for an aircraft state and associates binds to its corresponding dependencies.
     *
     * @param state The observable aircraft state.
     * @param group The group to which the trajectory will be added.
     */
    private void constructTrajectory(ObservableAircraftState state, Group group) {
        Group trajectory = new Group();
        trajectory.getStyleClass().add("trajectory");// associate style class with trajectory node
        group.getChildren().add(trajectory);
        // property visible must be bound to an expression that is only true when the selectedAircraft is the one to which the trajectory corresponds
        // Therefore the trajectory is only visible when the selectedAircraft is the one to which the trajectory corresponds
        trajectory.visibleProperty().bind(selectedAircraft.isEqualTo(state));
        // if zoom level changes, redraw the trajectory
        mapParameters.zoomLevelProperty().addListener((observable1, oldValue1, newValue1) -> drawTrajectory(state, trajectory));
        // if the trajectory changes, redraw the trajectory
        state.getTrajectory().addListener((ListChangeListener<ObservableAircraftState.AirbornePos>) c -> drawTrajectory(state, trajectory));
        // bind the layoutX and layoutY properties of the trajectory to the negation of the min x and min y properties of the map parameters
        trajectory.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectory.layoutYProperty().bind(mapParameters.minYProperty().negate());
    }

    /**
     * Draws the trajectory for an aircraft state on the given group.
     *
     * @param state      The observable aircraft state.
     * @param trajectory The group representing the trajectory.
     */
    private void drawTrajectory(ObservableAircraftState state, Group trajectory) {
        trajectory.getChildren().clear();

        List<ObservableAircraftState.AirbornePos> positions = state.getTrajectory();
        if (positions.isEmpty()) {
            return;
        }

        // gets the starting coordinates of the trajectory
        double x = WebMercator.x(mapParameters.getZoomLevel(), positions.get(0).pos().longitude());
        double y = WebMercator.y(mapParameters.getZoomLevel(), positions.get(0).pos().latitude());

        // Iterates over all the coordinates in the trajectory by setting a start and end coordinate then draws a line between them.
        // Provides the colour of the trajectory based on the altitude of the aircraft.
        for (int i = 0; i < positions.size() - 1; i++) {
            Line line = new Line();  // Create a new Line object for each line segment
            double endX = WebMercator.x(mapParameters.getZoomLevel(), positions.get(i + 1).pos().longitude());
            double endY = WebMercator.y(mapParameters.getZoomLevel(), positions.get(i + 1).pos().latitude());
            // Set the start and end coordinates of the line
            line.setStartX(x);
            line.setStartY(y);
            // Set the end coordinates of the line
            line.setEndX(endX);
            line.setEndY(endY);
            trajectory.getChildren().add(line);

            colourTrajectory(i, positions, x, y, endX, endY, line);
            x = endX;
            y = endY;
        }
    }

    /**
     * Colours the trajectory based on the altitude of the aircraft.
     *
     * @param i         The index of the current position in the trajectory.
     * @param positions The list of positions in the trajectory.
     * @param x         The x coordinate of the start of the line.
     * @param y         The y coordinate of the start of the line.
     * @param endX      The x coordinate of the end of the line.
     * @param endY      The y coordinate of the end of the line.
     * @param line      The line to be coloured.
     */
    private void colourTrajectory(int i, List<ObservableAircraftState.AirbornePos> positions, double x, double y,
                                  double endX, double endY, Line line) {
        double p1 = positions.get(i).altitude();
        double p2 = positions.get(i + 1).altitude();

        // Set the colour of the line based on the altitude of the aircraft
        // If the altitude between two points is the same it will be a constant colour, otherwise it will be a gradient.
        Color c1 = ColorRamp.PLASMA.at(Math.pow(p1 / maxAltitude, lowAltDefiner));
        if (p1 != p2) {
            Color c2 = ColorRamp.PLASMA.at(Math.pow(p2 / maxAltitude, lowAltDefiner));
            Stop s1 = new Stop(0, c1);
            Stop s2 = new Stop(1, c2);
            LinearGradient lg = new LinearGradient(x, y, endX, endY, true, NO_CYCLE, s1, s2);
            line.setStroke(lg);
        } else {
            line.setStroke(c1);
        }
    }

    /**
     * Removes a group from the pane based on its ID.
     *
     * @param groupID The ID of the group to be removed.
     */
    private void removeGroup(String groupID) {
        pane.getChildren().removeIf(group -> group.getId().equals(groupID));
    }

    /**
     * Calculates the screen coordinates for an aircraft state on the map based on the zoomLevel and top left corner of the map.
     *
     * @param state     The observable aircraft state.
     * @param mapParams The map parameters used for calculations.
     * @return The screen coordinates as a Point2D object.
     */
    private Point2D getScreenCoordinates(ObservableAircraftState state, MapParameters mapParams) {
        double x = WebMercator.x(mapParams.getZoomLevel(), state.getPosition().longitude()) - mapParams.getMinX();
        double y = WebMercator.y(mapParams.getZoomLevel(), state.getPosition().latitude()) - mapParams.getMinY();
        return new Point2D(x, y);
    }
}