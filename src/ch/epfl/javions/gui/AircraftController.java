package ch.epfl.javions.gui;/*
 * Author: Krish Chawla
 * Date:
 */

import ch.epfl.javions.WebMercator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public final class AircraftController {

    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final Pane pane;
    private final double maxAltitude = 12000;
    private final double lowAltDefiner = (double) 1/ 3;

    AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.selectedAircraft = selectedAircraft;
        this.states = states;
        this.mapParameters = mapParameters;
        pane = new Pane();

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

        // set the view order property to the negation of the altitude
        group.viewOrderProperty().bind(state.altitudeProperty().negate());

        // create the trajectory group and add it to the aircraft group
        Group trajectory = new Group();
        group.getChildren().add(trajectory);
        // TODO: 5/9/2023 when working on trajectory, format it using Bindings 

        // create the label and icon group and add it to the aircraft group
        Group labelIcon = new Group();
        group.getChildren().add(labelIcon);

        // create the aircraft icon and add it to the label and icon group
        labelIcon.getChildren().add(getAircraftIcon(state));

        // create the label and add it to the label and icon group
        Group label = new Group();
        labelIcon.getChildren().add(label);

        Rectangle background = new Rectangle();
        Text text = new Text();
        label.getChildren().add(background);
        label.getChildren().add(text);
        getLabel(state, background, text);
    }

    private SVGPath getAircraftIcon(ObservableAircraftState state) {
        SVGPath iconSVG = new SVGPath();
        AircraftIcon icon = AircraftIcon.iconFor(state.getAircraftData().typeDesignator(), state.getAircraftData().description(), state.getCategory(), state.getAircraftData().wakeTurbulenceCategory());
        icon.svgPath();

        // set rotation angle if the icon can rotate
        if (icon.canRotate()) {
            iconSVG.rotateProperty().bind(state.trackOrHeadingProperty());
        } else {
            iconSVG.rotateProperty().setValue(0);
        }

        // set fill color based on altitude
        iconSVG.fillProperty().bind(state.altitudeProperty().map(alt -> ColorRamp.PLASMA.at((Math.pow((double) alt / maxAltitude, lowAltDefiner)))));

        // position the icon/label group
        Point2D screenCoordinates = getScreenCoordinates(state, mapParameters);
        iconSVG.setLayoutX(screenCoordinates.getX());
        iconSVG.setLayoutY(screenCoordinates.getY());

        // TODO: 5/5/2023 check if correct style class
        iconSVG.getStyleClass().add("aircraft.css");

        return iconSVG;
    }

    private void getLabel(ObservableAircraftState state, Rectangle background, Text text) {
        // height should be bound to an expression whose value is equal to the height of the text of the label, plus 4.
        background.heightProperty().bind(text.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 4));

        // ensures text of altitude always is in metres
        text.textProperty().bind(Bindings.format("%f meters" , state.altitudeProperty()));

        //property visible must be bound to an expression that is only true when the zoom level is greater than or equal to 11 or selectedAircraft is one to which the label corresponds
        text.visibleProperty().bind(Bindings.createBooleanBinding(() -> mapParameters.getZoomLevel() >= 11 || selectedAircraft.get() == state, mapParameters.zoomLevelProperty(), selectedAircraft));
    }

    private void removeGroup(ObservableAircraftState state) {
        //remove the group from the pane
        // TODO: 5/8/2023 check if need to remove icon or entire state 
        states.remove(state);

    }

    private Point2D getScreenCoordinates(ObservableAircraftState state, MapParameters mapParams) {
        int zoomLevel = mapParams.getZoomLevel();
        double minX = mapParams.getMinX();
        double minY = mapParams.getMinY();
        double longitude = state.getPosition().longitude();
        double latitude = state.getPosition().latitude();

        double x = (WebMercator.x(zoomLevel,longitude) - minX);
        double y = (WebMercator.y(zoomLevel, latitude) - minY);

        return new Point2D(x, y);
    }
}
