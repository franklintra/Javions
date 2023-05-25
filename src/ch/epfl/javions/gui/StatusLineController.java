package ch.epfl.javions.gui;


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * @author @franklintra (362694)
 * @project Javions
 * The StatusLineController class manages the status line. It has a default constructor,
 * which builds the scene graph, and three public methods.
 */
public final class StatusLineController {
    private final BorderPane pane;
    private final IntegerProperty aircraftCount;
    private final LongProperty messageCount;

    /**
     * Default constructor builds the scene graph.
     */
    public StatusLineController() {
        this.pane = new BorderPane();
        this.pane.getStylesheets().add("status.css");

        this.aircraftCount = new SimpleIntegerProperty(0);
        this.messageCount = new SimpleLongProperty(0L);

        Text aircraftText = new Text();
        Text messageText = new Text();

        this.pane.setLeft(aircraftText);
        this.pane.setRight(messageText);

        aircraftText.textProperty().bind(this.aircraftCount.asString("Aircraft visible: %d"));
        messageText.textProperty().bind(this.messageCount.asString("Messages received: %d"));
    }

    /**
     * Returns the panel containing the status line.
     *
     * @return the BorderPane panel.
     */
    public BorderPane getPane() {
        return this.pane;
    }

    /**
     * Returns the modifiable property containing the number of currently visible aircraft.
     *
     * @return the aircraft count property.
     */
    public IntegerProperty aircraftCountProperty() {
        return this.aircraftCount;
    }

    /**
     * Returns the modifiable property containing the number of messages received since the beginning of program execution.
     *
     * @return the message count property.
     */
    public LongProperty messageCountProperty() {
        return this.messageCount;
    }
}
