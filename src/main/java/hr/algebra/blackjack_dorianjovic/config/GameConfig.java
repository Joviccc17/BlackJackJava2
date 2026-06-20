package hr.algebra.blackjack_dorianjovic.config;

import javafx.beans.property.*;

import java.io.Serializable;

public class GameConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient IntegerProperty numberOfDecks = new SimpleIntegerProperty(6);
    private final transient IntegerProperty startingBalance = new SimpleIntegerProperty(1000);
    private final transient IntegerProperty minBet = new SimpleIntegerProperty(10);
    private final transient IntegerProperty maxBet = new SimpleIntegerProperty(500);
    private final transient BooleanProperty dealerHitsSoft17 = new SimpleBooleanProperty(false);
    private final transient IntegerProperty serverPort = new SimpleIntegerProperty(12345);
    private final transient StringProperty serverHost = new SimpleStringProperty("localhost");
    private final transient IntegerProperty maxPlayers = new SimpleIntegerProperty(2);

    public int getNumberOfDecks() { return numberOfDecks.get(); }
    public void setNumberOfDecks(int value) { numberOfDecks.set(value); }

    public int getStartingBalance() { return startingBalance.get(); }
    public void setStartingBalance(int value) { startingBalance.set(value); }

    public int getMinBet() { return minBet.get(); }
    public void setMinBet(int value) { minBet.set(value); }

    public int getMaxBet() { return maxBet.get(); }
    public void setMaxBet(int value) { maxBet.set(value); }

    public boolean isDealerHitsSoft17() { return dealerHitsSoft17.get(); }
    public void setDealerHitsSoft17(boolean value) { dealerHitsSoft17.set(value); }

    public int getServerPort() { return serverPort.get(); }
    public void setServerPort(int value) { serverPort.set(value); }

    public String getServerHost() { return serverHost.get(); }
    public void setServerHost(String value) { serverHost.set(value); }

    public int getMaxPlayers() { return maxPlayers.get(); }
    public void setMaxPlayers(int value) { maxPlayers.set(value); }

    @Override
    public String toString() {
        return "GameConfig{" +
                "decks=" + getNumberOfDecks() +
                ", balance=" + getStartingBalance() +
                ", minBet=" + getMinBet() +
                ", maxBet=" + getMaxBet() +
                ", dealerHitsSoft17=" + isDealerHitsSoft17() +
                ", port=" + getServerPort() +
                ", host=" + getServerHost() +
                ", maxPlayers=" + getMaxPlayers() +
                '}';
    }
}
