package ch.epfl.javions.gui;/*
 * Author: Krish Chawla
 * Date:
 */
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.AircraftRegistration;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.awt.*;
import java.util.Set;

public final class AircraftController {

    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;

    private final Pane pane;

    AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.selectedAircraft = selectedAircraft;
        this.states = states;
        this.mapParameters = mapParameters;
        pane = new Pane();

    }

    public Pane pane() {
        return pane;
    }

    private void createPane () {
        states.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
            if(change.wasAdded()) {
                createGroup(change.getElementAdded());
            }
                });


    }
    private void createGroup(ObservableAircraftState state) {

        //main group for aircraft
        Group group = new Group();
        pane.getChildren().add(group);

        //group for trajectory
        Group trajectory = new Group();
        group.getChildren().add(trajectory);
        //group.getChildren().add();

        //group for label and icon
        Group labelIcon = new Group();
        group.getChildren().add(labelIcon);

        //icon
        SVGPath icon = new SVGPath();
        labelIcon.getChildren().add(icon);
        getAircraftIcon(state);

        //label
        Group label = new Group();
        labelIcon.getChildren().add(label);
        label.getChildren().add(new Rectangle());
        label.getChildren().add(new Text());

    }

    private void getAircraftIcon(ObservableAircraftState state) {
        AircraftIcon icon = AircraftIcon.iconFor(state.getAircraftData().typeDesignator(), state.getAircraftData().description(), state.getCategory(), state.getAircraftData().wakeTurbulenceCategory());
        icon.svgPath();
        //WebMercator, doubleBindings, calcualte x and y positions
        // TODO: 5/5/2023 style classes
    }

}
