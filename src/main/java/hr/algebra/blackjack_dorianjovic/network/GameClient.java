package hr.algebra.blackjack_dorianjovic.network;

import hr.algebra.blackjack_dorianjovic.model.PlayerAction;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * TCP Game Client that connects to the GameServer.
 * Sends player actions and receives game state updates.
 * Runs a background listener thread for incoming messages.
 */
public class GameClient {

    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected = false;

    private int playerId = -1;
    private String playerName;
    private Consumer<GameMessage> onMessageReceived;

    public GameClient(String host, int port, String playerName) {
        this.host = host;
        this.port = port;
        this.playerName = playerName;
    }

    /**
     * Connects to the server and starts the listener thread.
     */
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;

        // Start background listener thread
        Thread listenerThread = new Thread(this::listenForMessages, "Client-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Background loop that reads messages from the server.
     */
    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {
            // Server disconnected
        } catch (IOException | ClassNotFoundException e) {
            // Connection lost
        } finally {
            disconnect();
        }
    }

    /**
     * Processes incoming messages from the server.
     */
    private void handleMessage(GameMessage message) {
        switch (message.getType()) {
            case PLAYER_JOINED -> {
                // The server assigns player IDs in connection order
                if (playerId == -1) {
                    playerId = message.getPlayerId();
                }
            }
            case STATE_UPDATE, SHOWDOWN, GAME_START, CHAT, PLAYER_LEFT -> {
                // Forward to UI listener
            }
        }

        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    // ========================================================================
    // Send Methods
    // ========================================================================

    /**
     * Sends a bet to the server.
     */
    public void sendBet(int amount) {
        sendMessage(GameMessage.bet(playerId, playerName, amount));
    }

    /**
     * Sends a player action (HIT, STAND, etc.) to the server.
     */
    public void sendAction(PlayerAction action) {
        sendMessage(GameMessage.playerAction(playerId, playerName, action));
    }

    /**
     * Sends a chat message to the server.
     */
    public void sendChat(String text) {
        sendMessage(GameMessage.chat(playerId, playerName, text));
    }

    /**
     * Sends a deal request (start new round) to the server.
     */
    public void sendDealRequest() {
        sendMessage(GameMessage.dealRequest(playerId, playerName));
    }

    /**
     * Requests a state refresh from the server.
     */
    public void requestStateRefresh() {
        sendMessage(new GameMessage(MessageType.HEARTBEAT, playerId, playerName));
    }

    /**
     * Sends a message to the server.
     */
    private void sendMessage(GameMessage message) {
        try {
            if (out != null && connected) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("[Client] Error sending message: " + e.getMessage());
        }
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    // --- Getters/Setters ---

    public boolean isConnected() { return connected; }
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }

    public void setOnMessageReceived(Consumer<GameMessage> listener) {
        this.onMessageReceived = listener;
    }
}
