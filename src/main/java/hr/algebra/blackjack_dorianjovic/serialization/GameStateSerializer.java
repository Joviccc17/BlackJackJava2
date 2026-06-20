package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.model.GameState;

import java.io.*;
import java.nio.file.Path;

public class GameStateSerializer {

    public void saveGame(GameState gameState, Path filePath) throws IOException {

        File file = filePath.toFile();
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file))) {
            oos.writeObject(gameState);
        }
    }

    public GameState loadGame(Path filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            return (GameState) ois.readObject();
        }
    }
}
