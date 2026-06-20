package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private void onSinglePlayer() {
        try {
            GameController controller = SceneManager.getInstance()
                    .switchSceneAndGetController("game-view.fxml");
            controller.initSinglePlayer();
        } catch (IOException e) {
            System.err.println("Failed to load game view: " + e.getMessage());
        }
    }

    @FXML
    private void onHostMultiplayer() {
        try {
            LobbyController controller = SceneManager.getInstance()
                    .switchSceneAndGetController("lobby-view.fxml");
            controller.initAsHost();
        } catch (IOException e) {
            System.err.println("Failed to load lobby view: " + e.getMessage());
        }
    }

    @FXML
    private void onJoinMultiplayer() {
        try {
            LobbyController controller = SceneManager.getInstance()
                    .switchSceneAndGetController("lobby-view.fxml");
            controller.initAsJoiner();
        } catch (IOException e) {
            System.err.println("Failed to load lobby view: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadGame() {
        try {
            SceneManager.getInstance().switchScene("load-game-view.fxml");
        } catch (IOException e) {
            System.err.println("Failed to load game view: " + e.getMessage());
        }
    }

    @FXML
    private void onSettings() {
        try {
            SceneManager.getInstance().switchScene("settings-view.fxml");
        } catch (IOException e) {
            System.err.println("Failed to load settings view: " + e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}
