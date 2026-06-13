package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents the complete state of a game.
 * This is the object that gets serialized/deserialized for save/load,
 * and sent over the network between server and clients.
 *
 * In SINGLE_PLAYER mode: player1 + dealer are active, player2 is null.
 * In MULTIPLAYER mode: player1 + player2 are active, dealer is null.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private GameMode mode;
    private GamePhase phase;
    private Deck deck;

    // Single Player: player1 + dealer
    // Multiplayer: player1 + player2 (no dealer)
    private Player player1;
    private Player player2;
    private Dealer dealer;

    private int pot;           // MP only — combined stakes
    private int roundNumber;
    private String resultMessage;
    private LocalDateTime timestamp;

    public GameState(GameMode mode) {
        this.mode = mode;
        this.phase = GamePhase.WAITING;
        this.pot = 0;
        this.roundNumber = 0;
        this.timestamp = LocalDateTime.now();
    }

    // --- Game Mode ---
    public GameMode getMode() { return mode; }
    public void setMode(GameMode mode) { this.mode = mode; }

    // --- Game Phase ---
    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    // --- Deck ---
    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }

    // --- Player 1 ---
    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player1) { this.player1 = player1; }

    // --- Player 2 (MP only) ---
    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player2) { this.player2 = player2; }

    // --- Dealer (SP only) ---
    public Dealer getDealer() { return dealer; }
    public void setDealer(Dealer dealer) { this.dealer = dealer; }

    // --- Pot (MP only) ---
    public int getPot() { return pot; }
    public void setPot(int pot) { this.pot = pot; }
    public void addToPot(int amount) { this.pot += amount; }

    // --- Round Number ---
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public void incrementRound() { this.roundNumber++; }

    // --- Result Message ---
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    // --- Timestamp ---
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /**
     * Returns true if this is a single-player game.
     */
    public boolean isSinglePlayer() {
        return mode == GameMode.SINGLE_PLAYER;
    }

    /**
     * Returns true if this is a multiplayer game.
     */
    public boolean isMultiplayer() {
        return mode == GameMode.MULTIPLAYER;
    }

    @Override
    public String toString() {
        return "GameState{mode=" + mode +
                ", phase=" + phase +
                ", round=" + roundNumber +
                ", pot=" + pot +
                "}";
    }
}

