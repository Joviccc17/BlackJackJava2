package hr.algebra.blackjack_dorianjovic.view;

import hr.algebra.blackjack_dorianjovic.BlackjackApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Singleton utility for switching between FXML scenes on the primary Stage.
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Switch the current scene to the given FXML view.
     * @param fxmlFile the FXML filename (e.g., "main-menu-view.fxml")
     */
    public void switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                BlackjackApplication.class.getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, primaryStage.getScene().getWidth(),
                primaryStage.getScene().getHeight());
        primaryStage.setScene(scene);
    }

    /**
     * Switch scene and return the controller for further configuration.
     * @param fxmlFile the FXML filename
     * @param <T> the controller type
     * @return the controller instance
     */
    public <T> T switchSceneAndGetController(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                BlackjackApplication.class.getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, primaryStage.getScene().getWidth(),
                primaryStage.getScene().getHeight());
        primaryStage.setScene(scene);
        return loader.getController();
    }
}

