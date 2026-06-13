package hr.algebra.blackjack_dorianjovic.network;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.engine.GameEngine;
import hr.algebra.blackjack_dorianjovic.model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TCP Game Server for multiplayer BlackJack.
 * Runs the authoritative GameEngine, accepts client connections,
 * receives player actions, and broadcasts filtered game state updates.
 * Each client connection runs on its own thread.
 */
public class GameServer {

    private final int port;
    private final GameConfig config;
    private GameEngine engine;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;
    private GameServerListener listener;

    public GameServer(int port, GameConfig config) {
        this.port = port;
        this.config = config;
    }

    /**
     * Starts the server: opens the socket, initializes the engine,
     * then accepts client connections until maxPlayers is reached.
     * Blocks until all players have connected.
     * Call onReady.run() right after the server socket is open so the host
     * can connect as a client.
     */
    public void start(Runnable onReady) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        // Initialize multiplayer game engine
        engine = new GameEngine(config, GameMode.MULTIPLAYER);

        // Notify caller that the server socket is open and accepting
        if (onReady != null) {
            onReady.run();
        }

        while (running && clients.size() < config.getMaxPlayers()) {
            try {
                Socket clientSocket = serverSocket.accept();
                int playerId = clients.size() + 1;
                ClientHandler handler = new ClientHandler(clientSocket, playerId);
                clients.add(handler);

                Thread clientThread = new Thread(handler, "Client-" + playerId);
                clientThread.setDaemon(true);
                clientThread.start();

                // Assign player ID on the server-side engine
                if (playerId == 1) {
                    engine.getGameState().getPlayer1().setPlayerId(1);
                } else if (playerId == 2) {
                    engine.getGameState().getPlayer2().setPlayerId(2);
                }

                broadcastMessage(GameMessage.playerJoined(playerId, "Player " + playerId));

                if (clients.size() == config.getMaxPlayers()) {
                    engine.startNewRound();

                    // Brief delay to ensure all client listener threads are ready
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                    broadcastMessage(GameMessage.gameStart());
                    broadcastStateToAll();
                }
            } catch (IOException e) {
                if (running) {
                    // Connection error — continue accepting
                }
            }
        }
    }

    /**
     * Processes an incoming message from a client.
     */
    private synchronized void handleMessage(GameMessage message, int playerId) {
        switch (message.getType()) {
            case BET -> {
                Player player = getPlayer(playerId);
                if (player != null) {
                    try {
                        engine.placeBet(player, message.getBetAmount());
                        if (bothPlayersBet()) {
                            engine.dealInitialCards();
                        }
                        broadcastStateToAll();
                    } catch (Exception e) {
                        // Bet validation failed — ignore invalid bet
                    }
                }
            }
            case PLAYER_ACTION -> {
                Player player = getPlayer(playerId);
                if (player != null) {
                    try {
                        switch (message.getAction()) {
                            case HIT -> engine.playerHit(player);
                            case STAND -> engine.playerStand(player);
                            case DOUBLE_DOWN -> engine.playerDoubleDown(player);
                            case SPLIT -> engine.playerSplit(player);
                        }

                        if (engine.getGameState().getPhase() == GamePhase.SHOWDOWN) {
                            engine.resolveShowdown();
                            broadcastMessage(GameMessage.showdown(engine.getGameState()));
                        }

                        broadcastStateToAll();
                    } catch (Exception e) {
                        // Action validation failed — ignore invalid action
                    }
                }
            }
            case CHAT -> {
                // Relay chat to all clients
                broadcastMessage(message);
            }
            case DEAL_REQUEST -> {
                // Start a new round
                engine.startNewRound();
                broadcastStateToAll();
            }
            case HEARTBEAT -> {
                // Client requests current state refresh
                for (ClientHandler client : clients) {
                    if (client.playerId == playerId) {
                        GameState filtered = engine.getVisibleStateForPlayer(playerId);
                        client.sendMessage(GameMessage.stateUpdate(filtered));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Checks if both players have placed their bets.
     */
    private boolean bothPlayersBet() {
        Player p1 = engine.getGameState().getPlayer1();
        Player p2 = engine.getGameState().getPlayer2();
        return p1 != null && p2 != null
                && p1.getCurrentBet() > 0 && p2.getCurrentBet() > 0;
    }

    /**
     * Gets the player object for the given player ID.
     */
    private Player getPlayer(int playerId) {
        if (playerId == 1) return engine.getGameState().getPlayer1();
        if (playerId == 2) return engine.getGameState().getPlayer2();
        return null;
    }

    /**
     * Sends a filtered game state to each client (opponent cards hidden).
     */
    private void broadcastStateToAll() {
        for (ClientHandler client : clients) {
            GameState filtered = engine.getVisibleStateForPlayer(client.playerId);
            client.sendMessage(GameMessage.stateUpdate(filtered));
        }
    }

    /**
     * Sends a message to all connected clients.
     */
    private void broadcastMessage(GameMessage message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     * Stops the server and disconnects all clients.
     */
    public void stop() {
        running = false;
        for (ClientHandler client : clients) {
            client.disconnect();
        }
        clients.clear();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }

    public boolean isRunning() { return running; }
    public GameEngine getEngine() { return engine; }

    public void setListener(GameServerListener listener) {
        this.listener = listener;
    }

    // ========================================================================
    // Client Handler — one per connected client, runs on its own thread
    // ========================================================================

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;
        private volatile boolean connected = false;

        ClientHandler(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            // Initialize streams immediately so they're ready before broadcast
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
            this.connected = true;
        }

        @Override
        public void run() {
            try {
                while (connected && running) {
                    GameMessage message = (GameMessage) in.readObject();
                    handleMessage(message, playerId);
                }
            } catch (EOFException e) {
                // Client disconnected normally
            } catch (IOException | ClassNotFoundException e) {
                // Connection lost
            } finally {
                disconnect();
                clients.remove(this);
                broadcastMessage(GameMessage.playerLeft(playerId, "Player " + playerId));
                if (listener != null) {
                    listener.onPlayerDisconnected(playerId);
                }
            }
        }

        void sendMessage(GameMessage message) {
            try {
                if (connected) {
                    synchronized (out) {
                        out.writeObject(message);
                        out.flush();
                        out.reset();
                    }
                }
            } catch (IOException ignored) {}
        }

        void disconnect() {
            connected = false;
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Listener interface for server events.
     */
    public interface GameServerListener {
        void onPlayerDisconnected(int playerId);
    }
}

