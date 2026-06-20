package hr.algebra.blackjack_dorianjovic.threading;

import hr.algebra.blackjack_dorianjovic.model.GameState;
import hr.algebra.blackjack_dorianjovic.serialization.SaveManager;
import javafx.concurrent.Task;

public class GameSaveTask extends Task<Boolean> {

    private final GameState gameState;
    private final int slot;
    private final SaveManager saveManager;

    public GameSaveTask(GameState gameState, int slot, SaveManager saveManager) {
        this.gameState = gameState;
        this.slot = slot;
        this.saveManager = saveManager;
    }

    @Override
    protected Boolean call() throws Exception {
        updateMessage("Saving game...");
        updateProgress(0, 1);

        saveManager.saveToSlot(gameState, slot);

        updateProgress(1, 1);
        updateMessage("Game saved to slot " + slot);
        return true;
    }
}
