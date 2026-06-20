package hr.algebra.blackjack_dorianjovic.view;

import hr.algebra.blackjack_dorianjovic.BlackjackApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

    public void switchScene(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                BlackjackApplication.class.getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root, primaryStage.getScene().getWidth(),
                primaryStage.getScene().getHeight());
        primaryStage.setScene(scene);
    }

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
