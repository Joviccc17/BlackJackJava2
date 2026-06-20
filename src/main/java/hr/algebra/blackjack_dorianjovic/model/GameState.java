package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private GameMode mode;
    private GamePhase phase;
    private Deck deck;

    private Player player1;
    private Player player2;
    private Dealer dealer;

    private int pot;
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

    public GameMode getMode() { return mode; }

    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }

    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player1) { this.player1 = player1; }

    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player2) { this.player2 = player2; }

    public Dealer getDealer() { return dealer; }
    public void setDealer(Dealer dealer) { this.dealer = dealer; }

    public int getPot() { return pot; }
    public void setPot(int pot) { this.pot = pot; }
    public void addToPot(int amount) { this.pot += amount; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public void incrementRound() { this.roundNumber++; }

    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isSinglePlayer() {
        return mode == GameMode.SINGLE_PLAYER;
    }

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
