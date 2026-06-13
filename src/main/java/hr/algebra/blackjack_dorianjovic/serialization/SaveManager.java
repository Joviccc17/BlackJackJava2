package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.model.GameState;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages save files in a "saves/" directory.
 * Provides methods to list, save, load, and delete saved games.
 */
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

    /**
     * Creates the saves directory if it doesn't exist.
     */
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

    /**
     * Saves the current game state to the specified slot.
     *
     * @param gameState the game state to save
     * @param slot      the slot number (1 to MAX_SAVE_SLOTS)
     * @throws IOException if saving fails
     */
    public void saveToSlot(GameState gameState, int slot) throws IOException {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            throw new IllegalArgumentException("Slot must be between 1 and " + MAX_SAVE_SLOTS);
        }

        gameState.setTimestamp(LocalDateTime.now());
        Path filePath = getSlotPath(slot);
        serializer.saveGame(gameState, filePath);
    }

    /**
     * Loads a game state from the specified slot.
     *
     * @param slot the slot number to load from
     * @return the restored GameState
     * @throws IOException            if loading fails
     * @throws ClassNotFoundException if deserialization fails
     */
    public GameState loadFromSlot(int slot) throws IOException, ClassNotFoundException {
        Path filePath = getSlotPath(slot);
        if (!Files.exists(filePath)) {
            throw new IOException("No save found in slot " + slot);
        }
        return serializer.loadGame(filePath);
    }

    /**
     * Returns a list of all available save slots with metadata.
     */
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
                    // Corrupted save file — skip it
                    System.err.println("Corrupted save in slot " + slot + ": " + e.getMessage());
                }
            }
        }

        saves.sort(Comparator.comparingInt(SaveSlot::slot));
        return saves;
    }

    /**
     * Deletes the save in the specified slot.
     */
    public boolean deleteSlot(int slot) {
        try {
            Path filePath = getSlotPath(slot);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns true if a save exists in the given slot.
     */
    public boolean slotExists(int slot) {
        return Files.exists(getSlotPath(slot));
    }

    /**
     * Returns the file path for a given slot number.
     */
    private Path getSlotPath(int slot) {
        return Paths.get(SAVES_DIR, SAVE_FILE_PREFIX + slot + SAVE_FILE_EXTENSION);
    }

    public int getMaxSaveSlots() {
        return MAX_SAVE_SLOTS;
    }
}

