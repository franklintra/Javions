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
 * This class represents the controller for the aircraft table
 * It is responsible for creating the table and its columns
 * It also sets up the listeners for the table
 * It offers a public method to get the JavaFX final table (getPane)
 */
public class AircraftTableController {
    private static final int ICAO_COLUMN_WIDTH = 60;
    private static final int CALLSIGN_COLUMN_WIDTH = 70;
    private static final int REGISTRATION_COLUMN_WIDTH = 90;
    private static final int MODEL_COLUMN_WIDTH = 230;
    private static final int TYPE_COLUMN_WIDTH = 50;
    private static final int DESCRIPTION_COLUMN_WIDTH = 70;
    private static final int NUMERIC_COLUMNS_WIDTH = 85;
    private final TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;

    /**
     * The constructor of the controller
     *
     * @param aircraftStates   is the set of aircraft states that will be displayed in the table (ObservableSet)
     * @param selectedAircraft is the property that will be updated when a row is selected (ObjectProperty)
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates, ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.aircraftStates = aircraftStates;
        this.selectedAircraft = selectedAircraft;
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        tableView.getStylesheets().add("table.css");
        createColumns();
        setUpListeners();
    }

    /**
     * @return the javafx pane representing the table
     */
    public TableView<ObservableAircraftState> getPane() {
        return tableView;
    }

    /**
     * This public method allows the class user to set up a listener on double click of a row (aircraft)
     *
     * @param clickOn is the consumer that will be called when a row is double-clicked
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> clickOn) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                if (selectedAircraft != null && clickOn != null) {
                    clickOn.accept(selectedAircraft.get());
                }
            }
        });
    }

    /**
     * This method creates all the columns of the table
     * It uses the private createTextColumn and createNumericColumn methods to do so to avoid code duplication
     */
    private void createColumns() {
        // Text columns
        createTextColumn("OACI", ICAO_COLUMN_WIDTH, state -> new ReadOnlyStringWrapper(state.getIcaoAddress().string()));
        createTextColumn("Indicatif", CALLSIGN_COLUMN_WIDTH, state -> state.callSignProperty().map(CallSign::string));
        createTextColumn("Immatriculation", REGISTRATION_COLUMN_WIDTH, state -> new ReadOnlyStringWrapper(state.aircraftData().registration().string()));
        createTextColumn("Modèle", MODEL_COLUMN_WIDTH, state -> new ReadOnlyStringWrapper(state.aircraftData().model()));
        createTextColumn("Type", TYPE_COLUMN_WIDTH, state -> new ReadOnlyStringWrapper(state.aircraftData().typeDesignator().string()));
        createTextColumn("Description", DESCRIPTION_COLUMN_WIDTH, state -> new ReadOnlyStringWrapper(state.aircraftData().description().string()));

        // Numeric columns
        createNumericColumn("Longitude (°)", 4, state -> state.positionProperty().map(position -> Units.convertTo(position.longitude(), Units.Angle.DEGREE)));
        createNumericColumn("Latitude (°)", 4, state -> state.positionProperty().map(position -> Units.convertTo(position.latitude(), Units.Angle.DEGREE)));
        createNumericColumn("Altitude (m)", 0, ObservableAircraftState::altitudeProperty);
        createNumericColumn("Vitesse (km/h)", 0, state -> state.velocityProperty().map(speed -> Units.convertTo((Double) speed, Units.Speed.KILOMETER_PER_HOUR)));
    }

    /**
     * This method sets up the listeners for the table
     * For example when an aircraft is selected, it is highlighted in the table
     * When an aircraft is added or removed, it is added or removed from the table
     */
    private void setUpListeners() {
        // When an aircraft is added or removed from the set, it is added or removed from the table
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) item -> {
            if (item.wasAdded()) {
                tableView.getItems().add(item.getElementAdded());
                tableView.sort();
            }
            if (item.wasRemoved()) {
                tableView.getItems().remove(item.getElementRemoved());
            }
        });

        // When an aircraft is selected in the map, it is selected in the table
        selectedAircraft.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tableView.getSelectionModel().select(newValue);
                tableView.scrollTo(newValue);
            } else {
                tableView.getSelectionModel().clearSelection();
            }
        });

        // When the user selects an aircraft in the table, it is selected in the map
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedAircraft.set(newValue);
            }
        });
    }


    /**
     * This method helps the programmer create a text column.
     * It then adds it directly to the table
     *
     * @param title            The title of the column
     * @param prefWidth        The preferred width of the column
     * @param cellValueFactory The function that returns the value of the cell from the ObservableAircraftState
     */
    private void createTextColumn(String title, int prefWidth,
                                  Function<ObservableAircraftState, ObservableValue<String>> cellValueFactory) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(cellData -> cellValueFactory.apply(cellData.getValue()));
        tableView.getColumns().add(column);
    }

    /**
     * This method helps the programmer create a numeric column with a certain number of decimal places.
     * It then adds it directly to the table
     *
     * @param title            The title of the column
     * @param decimalPlaces    The number of decimal places
     * @param cellValueFactory The function that returns the value of the cell from the ObservableAircraftState
     */
    private void createNumericColumn(String title, int decimalPlaces,
                                     Function<ObservableAircraftState, ObservableValue<Number>> cellValueFactory) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.getStyleClass().add("numeric");
        NumberFormat format = NumberFormat.getInstance();
        column.setPrefWidth(NUMERIC_COLUMNS_WIDTH); // this is for all numeric columns
        column.setCellValueFactory(cellData -> {
            ObservableValue<Number> numberValue = cellValueFactory.apply(cellData.getValue());
            format.setMinimumFractionDigits(decimalPlaces);
            format.setMaximumFractionDigits(decimalPlaces);
            return Bindings.createStringBinding(() -> {
                Number value = numberValue.getValue();
                return value != null ? format.format(value) : "";
            }, numberValue);
        });

        column.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            }
            try {
                Number n1 = format.parse(s1);
                Number n2 = format.parse(s2);
                return Double.compare(n1.doubleValue(), n2.doubleValue());
            } catch (ParseException e) {
                // This should never happen but the try-catch structure is required by the parse method
                throw new RuntimeException(e);
            }
        });
        tableView.getColumns().add(column);
    }
}