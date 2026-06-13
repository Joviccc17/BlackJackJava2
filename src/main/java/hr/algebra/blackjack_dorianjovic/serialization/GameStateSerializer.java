package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.model.GameState;

import java.io.*;
import java.nio.file.Path;

/**
 * Handles saving and loading GameState objects using Java serialization.
 * Uses ObjectOutputStream for serialization and ObjectInputStream for deserialization.
 */
public class GameStateSerializer {

    /**
     * Serializes a GameState object to a file on disk.
     *
     * @param gameState the game state to save
     * @param filePath  the file path to write to
     * @throws IOException if an I/O error occurs during writing
     */
    public void saveGame(GameState gameState, Path filePath) throws IOException {
        // Ensure parent directories exist
        File file = filePath.toFile();
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file))) {
            oos.writeObject(gameState);
        }
    }

    /**
     * Deserializes a GameState object from a file on disk.
     *
     * @param filePath the file path to read from
     * @return the restored GameState
     * @throws IOException            if an I/O error occurs during reading
     * @throws ClassNotFoundException if the GameState class is not found
     */
    public GameState loadGame(Path filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            return (GameState) ois.readObject();
        }
    }
}

