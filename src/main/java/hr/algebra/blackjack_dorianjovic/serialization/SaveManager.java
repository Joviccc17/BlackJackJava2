package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.model.GameState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SaveManager {

    private static final String SAVES_DIR = "saves";
    private static final String SAVE_FILE_PREFIX = "blackjack_save_";
    private static final String SAVE_FILE_EXTENSION = ".dat";
    private static final int MAX_SAVE_SLOTS = 5;

    private final GameStateSerializer serializer;

    public SaveManager() {
        this.serializer = new GameStateSerializer();
        ensureSavesDirectory();
    }

    private void ensureSavesDirectory() {
        Path savesPath = Paths.get(SAVES_DIR);
        if (!Files.exists(savesPath)) {
            try {
                Files.createDirectories(savesPath);
            } catch (IOException e) {
                System.err.println("Failed to create saves directory: " + e.getMessage());
            }
        }
    }

    public void saveToSlot(GameState gameState, int slot) throws IOException {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Slot must be between 1 and " + MAX_SAVE_SLOTS);
        }

        gameState.setTimestamp(LocalDateTime.now());
        Path filePath = getSlotPath(slot);
        serializer.saveGame(gameState, filePath);
    }

    public GameState loadFromSlot(int slot) throws IOException, ClassNotFoundException {
        Path filePath = getSlotPath(slot);
        if (!Files.exists(filePath)) {
            throw new IOException("No save found in slot " + slot);
        }
        return serializer.loadGame(filePath);
    }

    public List<SaveSlot> getAvailableSaves() {
        List<SaveSlot> saves = new ArrayList<>();

        for (int slot = 1; slot <= MAX_SAVE_SLOTS; slot++) {
            Path filePath = getSlotPath(slot);
            if (Files.exists(filePath)) {
                try {
                    GameState state = serializer.loadGame(filePath);
                    saves.add(new SaveSlot(
                            slot,
                            state.getPlayer1().getName(),
                            state.getPlayer1().getChips(),
                            state.getRoundNumber(),
                            state.getMode().name(),
                            state.getTimestamp() != null ? state.getTimestamp() : LocalDateTime.now(),
                            filePath
                    ));
                } catch (Exception e) {

                    System.err.println("Corrupted save in slot " + slot + ": " + e.getMessage());
                }
            }
        }

        saves.sort(Comparator.comparingInt(SaveSlot::slot));
        return saves;
    }

    public boolean deleteSlot(int slot) {
        try {
            Path filePath = getSlotPath(slot);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    private Path getSlotPath(int slot) {
        return Paths.get(SAVES_DIR, SAVE_FILE_PREFIX + slot + SAVE_FILE_EXTENSION);
    }
}
