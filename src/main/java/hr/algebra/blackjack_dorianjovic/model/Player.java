package hr.algebra.blackjack_dorianjovic.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.Serializable;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Hand hand;
    private Hand splitHand;
    private int chips;
    private int currentBet;
    private int playerId;

    private transient IntegerProperty chipsProperty;
    private transient IntegerProperty currentBetProperty;

    public Player(String name, int startingChips) {
        this.name = name;
        this.chips = startingChips;
        this.hand = new Hand();
        this.splitHand = null;
        this.currentBet = 0;
        this.playerId = -1;
    }

    public String getName() { return name; }

    public Hand getHand() { return hand; }

    public Hand getSplitHand() { return splitHand; }
    public void setSplitHand(Hand splitHand) { this.splitHand = splitHand; }
    public boolean hasSplit() { return splitHand != null; }

    public IntegerProperty chipsProperty() {
        if (chipsProperty == null) chipsProperty = new SimpleIntegerProperty(chips);
        return chipsProperty;
    }

    public int getChips() { return chipsProperty().get(); }

    public void setChips(int value) {
        this.chips = value;
        chipsProperty().set(value);
    }

    public void addChips(int amount) { setChips(getChips() + amount); }
    public void removeChips(int amount) { setChips(getChips() - amount); }

    public IntegerProperty currentBetProperty() {
        if (currentBetProperty == null) currentBetProperty = new SimpleIntegerProperty(currentBet);
        return currentBetProperty;
    }

    public int getCurrentBet() { return currentBetProperty().get(); }

    public void setCurrentBet(int value) {
        this.currentBet = value;
        currentBetProperty().set(value);
    }

    public void placeBet(int amount) {
        if (amount > getChips()) {
            throw new IllegalArgumentException("Insufficient chips. Have: " + getChips() + ", bet: " + amount);
        }
        setChips(getChips() - amount);
        setCurrentBet(amount);
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public void resetForNewRound() {
        hand.clear();
        splitHand = null;
        setCurrentBet(0);
    }

    @Override
    public String toString() {
        return name + " [chips=" + chips + ", bet=" + currentBet + ", hand=" + hand + "]";
    }
}
