package hr.algebra.blackjack_dorianjovic.serialization;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents a single save slot with metadata about the saved game.
 */
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

    /**
     * Returns a formatted display string for the save slot.
     */
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

