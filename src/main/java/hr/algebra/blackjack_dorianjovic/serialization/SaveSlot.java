package hr.algebra.blackjack_dorianjovic.serialization;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

public record SaveSlot(
        int slot,
        String playerName,
        int chips,
        int roundNumber,
        String gameMode,
        LocalDateTime timestamp,
        Path filePath
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getDisplayText() {
        return String.format("Slot %d — %s | Chips: %d | Round: %d | %s | %s",
                slot, playerName, chips, roundNumber, gameMode,
                timestamp.toString().replace("T", " "));
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}
