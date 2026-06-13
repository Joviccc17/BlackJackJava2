package hr.algebra.blackjack_dorianjovic.network;

import hr.algebra.blackjack_dorianjovic.model.GameState;
import hr.algebra.blackjack_dorianjovic.model.PlayerAction;

import java.io.Serializable;

/**
 * Serializable message wrapper sent between server and clients over TCP.
 * Contains the message type, optional payload data, and sender info.
 */
public class GameMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MessageType type;
    private final int playerId;
    private final String senderName;

    // Optional payload fields — only the relevant ones are set per message type
    private PlayerAction action;
    private GameState gameState;
    private String chatText;
    private int betAmount;

    public GameMessage(MessageType type, int playerId, String senderName) {
        this.type = type;
        this.playerId = playerId;
        this.senderName = senderName;
    }

    // --- Static factory methods for common message types ---

    public static GameMessage playerAction(int playerId, String name, PlayerAction action) {
        GameMessage msg = new GameMessage(MessageType.PLAYER_ACTION, playerId, name);
        msg.action = action;
        return msg;
    }

    public static GameMessage bet(int playerId, String name, int amount) {
        GameMessage msg = new GameMessage(MessageType.BET, playerId, name);
        msg.betAmount = amount;
        return msg;
    }

    public static GameMessage stateUpdate(GameState state) {
        GameMessage msg = new GameMessage(MessageType.STATE_UPDATE, 0, "Server");
        msg.gameState = state;
        return msg;
    }

    public static GameMessage chat(int playerId, String name, String text) {
        GameMessage msg = new GameMessage(MessageType.CHAT, playerId, name);
        msg.chatText = text;
        return msg;
    }

    public static GameMessage playerJoined(int playerId, String name) {
        return new GameMessage(MessageType.PLAYER_JOINED, playerId, name);
    }

    public static GameMessage playerLeft(int playerId, String name) {
        return new GameMessage(MessageType.PLAYER_LEFT, playerId, name);
    }

    public static GameMessage gameStart() {
        return new GameMessage(MessageType.GAME_START, 0, "Server");
    }

    public static GameMessage dealRequest(int playerId, String name) {
        return new GameMessage(MessageType.DEAL_REQUEST, playerId, name);
    }

    public static GameMessage showdown(GameState fullState) {
        GameMessage msg = new GameMessage(MessageType.SHOWDOWN, 0, "Server");
        msg.gameState = fullState;
        return msg;
    }

    // --- Getters ---

    public MessageType getType() { return type; }
    public int getPlayerId() { return playerId; }
    public String getSenderName() { return senderName; }
    public PlayerAction getAction() { return action; }
    public GameState getGameState() { return gameState; }
    public String getChatText() { return chatText; }
    public int getBetAmount() { return betAmount; }

    @Override
    public String toString() {
        return "GameMessage{type=" + type + ", playerId=" + playerId + ", sender=" + senderName + "}";
    }
}

