package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author @franklintra (362694)
 * @project Javions
 */

public class AircraftTableController {
    private final TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates, ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.aircraftStates = aircraftStates;
        this.selectedAircraft = selectedAircraft;
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        //tableView.getStylesheets().add("table.css");
        createColumns();
        setUpListeners();
    }

    public TableView<ObservableAircraftState> getPane() {
        return tableView;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> clickOn) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                if (selectedAircraft != null && clickOn != null) {
                    clickOn.accept(selectedAircraft.get());
                }
            }
        });
    }

    private void setUpListeners() {
        selectedAircraft.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tableView.getSelectionModel().select(newValue);
                tableView.scrollTo(newValue);
            } else {
                tableView.getSelectionModel().clearSelection();
            }
        });

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) item -> {
            if (item.wasAdded()) {
                tableView.getItems().add(item.getElementAdded());
                tableView.sort();
            }
            if (item.wasRemoved()) {
                tableView.getItems().remove(item.getElementRemoved());
            }
        });
    }

    private static TableColumn<ObservableAircraftState, String> createTextColumn(String title, int prefWidth,
                                                                          Function<ObservableAircraftState, ObservableValue<String>> cellValueFactory) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(cellData -> {
            return cellValueFactory.apply(cellData.getValue());
        });
        return column;
    }

    private TableColumn<ObservableAircraftState, String> createNumericColumn(String title, int prefWidth, int decimalPlaces,
                                                                             Function<ObservableAircraftState, ObservableValue<Number>> cellValueFactory) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        NumberFormat format = NumberFormat.getInstance();
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(cellData -> {
            ObservableValue<Number> numberValue = cellValueFactory.apply(cellData.getValue());
            format.setMinimumFractionDigits(decimalPlaces);
            format.setMaximumFractionDigits(decimalPlaces);
            return Bindings.createStringBinding(() -> {
                Number value = numberValue.getValue();
                return value != null ? format.format(value) : "";
        }, numberValue);});

        column.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            }
            try {
                Number n1 = format.parse(s1);
                Number n2 = format.parse(s2);
                return Double.compare(n1.doubleValue(), n2.doubleValue());
            } catch (ParseException e) {
                e.printStackTrace(System.err);
                throw new IllegalArgumentException(e);
            }
        });

        return column;
    }

    private void createColumns() {
        // Text columns
        tableView.getColumns().add(createTextColumn("OACI", 60, state -> new ReadOnlyStringWrapper(state.getIcaoAddress().string())));
        tableView.getColumns().add(createTextColumn("Indicatif", 70, state -> state.callSignProperty().map(CallSign::string)));
        tableView.getColumns().add(createTextColumn("Immatriculation", 90, state -> new ReadOnlyStringWrapper(state.aircraftData().registration().string())));
        tableView.getColumns().add(createTextColumn("Modèle", 90, state -> new ReadOnlyStringWrapper(state.aircraftData().model())));
        tableView.getColumns().add(createTextColumn("Type", 90, state -> new ReadOnlyStringWrapper(state.aircraftData().typeDesignator().string())));
        // Numeric columns
        tableView.getColumns().add(createNumericColumn("Longitude (°)", 85, 4, state -> state.positionProperty().map(position -> Units.convertTo(position.longitude(), Units.Angle.DEGREE))));
        tableView.getColumns().add(createNumericColumn("Latitude (°)", 85, 4, state -> state.positionProperty().map(position -> Units.convertTo(position.latitude(), Units.Angle.DEGREE))));
        tableView.getColumns().add(createNumericColumn("Altitude (m)", 85, 0, ObservableAircraftState::altitudeProperty));
        tableView.getColumns().add(createNumericColumn("Vitesse (km/h)", 85, 0, state -> state.velocityProperty().map(speed -> Units.convertTo((Double) speed, Units.Speed.KILOMETER_PER_HOUR))));
    }
}