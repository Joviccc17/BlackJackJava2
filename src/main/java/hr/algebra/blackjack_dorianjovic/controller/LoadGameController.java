package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.engine.*;
import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.serialization.ReflectionDocGenerator;
import hr.algebra.blackjack_dorianjovic.serialization.SaveManager;
import hr.algebra.blackjack_dorianjovic.serialization.SaveSlot;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class LoadGameController {

    @FXML private Label lblLoadStatus;
    @FXML private ListView<SaveSlot> lstSaves;

    private final SaveManager saveManager = new SaveManager();

    @FXML
    public void initialize() {
        refreshSaveList();
    }

    private void refreshSaveList() {
        lstSaves.getItems().clear();
        List<SaveSlot> saves = saveManager.getAvailableSaves();

        if (saves.isEmpty()) {
            lblLoadStatus.setText("No saved games found.");
        } else {
            lstSaves.getItems().addAll(saves);
            lblLoadStatus.setText("Select a saved game to load:");
        }
    }

    @FXML
    private void onLoad() {
        SaveSlot selected = lstSaves.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblLoadStatus.setText("Please select a save slot first.");
            return;
        }

        try {
            GameState savedState = saveManager.loadFromSlot(selected.slot());

            GameController controller = SceneManager.getInstance()
                    .switchSceneAndGetController("game-view.fxml");
            controller.initFromSavedState(savedState);

            lblLoadStatus.setText("Game loaded successfully!");
        } catch (Exception e) {
            lblLoadStatus.setText("Failed to load: " + e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        SaveSlot selected = lstSaves.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblLoadStatus.setText("Please select a save slot to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete save in slot " + selected.slot() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Save");
        confirm.setHeaderText("Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                saveManager.deleteSlot(selected.slot());
                refreshSaveList();
                lblLoadStatus.setText("Save slot " + selected.slot() + " deleted.");
            }
        });
    }

    @FXML
    private void onGenerateDocs() {
        try {
            ReflectionDocGenerator generator = new ReflectionDocGenerator();
            generator.generateDocumentation("documentation.txt",

                    Card.class,
                    Deck.class,
                    Hand.class,
                    Player.class,
                    Dealer.class,
                    GameState.class,
                    Suit.class,
                    Rank.class,
                    GameMode.class,
                    GamePhase.class,
                    GameResult.class,
                    PlayerAction.class,

                    BlackjackRules.class,
                    GameEngine.class,
                    TurnManager.class
            );

            lblLoadStatus.setText("Documentation generated: documentation.txt");

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Documentation Generated");
            info.setHeaderText("Success!");
            info.setContentText("Documentation has been generated and saved to:\n"
                    + "documentation.txt\n\n"
                    + "The file contains class structures, fields, methods,\n"
                    + "and @Documented annotations for all model and engine classes.");
            info.show();

        } catch (IOException e) {
            lblLoadStatus.setText("Failed to generate docs: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        try {
            SceneManager.getInstance().switchScene("main-menu-view.fxml");
        } catch (IOException e) {
            lblLoadStatus.setText("Error returning to menu.");
        }
    }
}
