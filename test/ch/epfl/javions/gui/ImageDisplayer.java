package ch.epfl.javions.gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

/**
 * @author @franklintra (362694)
 * @project Javions
 */
public class ImageDisplayer {
    public static void display(Image image) {
        CountDownLatch latch = new CountDownLatch(1);
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage primaryStage = new Stage();
            primaryStage.setTitle("Image Display");

            ImageView imageView = new ImageView(image);
            StackPane root = new StackPane(imageView);
            Scene scene = new Scene(root, image.getWidth(), image.getHeight());

            primaryStage.setScene(scene);
            primaryStage.show();

            // Close the stage when the user closes the application
            primaryStage.setOnCloseRequest(event -> {
                latch.countDown();
                Platform.exit();
            });
        }, null);

        // Initialize JavaFX if it's not initialized yet
        new JFXPanel();
        Platform.runLater(task);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
