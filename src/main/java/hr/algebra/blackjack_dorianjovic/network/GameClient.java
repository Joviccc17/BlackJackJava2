package hr.algebra.blackjack_dorianjovic.network;

import hr.algebra.blackjack_dorianjovic.model.PlayerAction;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

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

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;

        Thread listenerThread = new Thread(this::listenForMessages, "Client-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {

        } catch (IOException | ClassNotFoundException e) {

        } finally {
            disconnect();
        }
    }

    private void handleMessage(GameMessage message) {
        switch (message.getType()) {
            case PLAYER_JOINED -> {

                if (playerId == -1) {
                    playerId = message.getPlayerId();
                }
            }
            case STATE_UPDATE, SHOWDOWN, GAME_START, CHAT, PLAYER_LEFT -> {

            }
        }

        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    public void sendBet(int amount) {
        sendMessage(GameMessage.bet(playerId, playerName, amount));
    }

    public void sendAction(PlayerAction action) {
        sendMessage(GameMessage.playerAction(playerId, playerName, action));
    }

    public void sendDealRequest() {
        sendMessage(GameMessage.dealRequest(playerId, playerName));
    }

    public void requestStateRefresh() {
        sendMessage(new GameMessage(MessageType.HEARTBEAT, playerId, playerName));
    }

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

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public void setOnMessageReceived(Consumer<GameMessage> listener) {
        this.onMessageReceived = listener;
    }
}
