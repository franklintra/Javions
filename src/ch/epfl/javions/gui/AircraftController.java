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
import javafx.scene.shape.SVGPath;

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
        Group group = new Group();
        pane.getChildren().add(group);
        Group trajectory = new Group();
        group.getChildren().add(trajectory);
        Group labelIcon = new Group();
        Group label = new Group();
        labelIcon.getChildren().add(labelIcon);
        SVGPath icon = new SVGPath();
        labelIcon.getChildren().add(icon);

       AircraftIcon icon1 = AircraftIcon.iconFor(state.getAircraftData().typeDesignator(), state.getAircraftData().description(), state.getCategory(), state.getAircraftData().wakeTurbulenceCategory());
        icon1.svgPath();
    }


}
