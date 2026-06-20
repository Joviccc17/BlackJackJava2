package hr.algebra.blackjack_dorianjovic.network;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.engine.GameEngine;
import hr.algebra.blackjack_dorianjovic.model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    public void start(Runnable onReady) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        engine = new GameEngine(config, GameMode.MULTIPLAYER);

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

                if (playerId == 1) {
                    engine.getGameState().getPlayer1().setPlayerId(1);
                } else if (playerId == 2) {
                    engine.getGameState().getPlayer2().setPlayerId(2);
                }

                broadcastMessage(GameMessage.playerJoined(playerId, "Player " + playerId));

                if (clients.size() == config.getMaxPlayers()) {
                    engine.startNewRound();

                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}

                    broadcastMessage(GameMessage.gameStart());
                    broadcastStateToAll();
                }
            } catch (IOException e) {
                if (running) {

                }
            }
        }
    }

    private synchronized void handleMessage(GameMessage message, int playerId) {
        switch (message.getType()) {
            case BET -> {
                Player player = getPlayer(playerId);
                System.out.println("[Server] BET received from player " + playerId
                        + ", amount=" + message.getBetAmount()
                        + ", player=" + (player != null ? player.getName() : "NULL"));
                if (player != null) {
                    try {
                        engine.placeBet(player, message.getBetAmount());
                        System.out.println("[Server] Bet placed successfully. P1 bet="
                                + engine.getGameState().getPlayer1().getCurrentBet()
                                + ", P2 bet="
                                + engine.getGameState().getPlayer2().getCurrentBet());
                        if (bothPlayersBet()) {
                            System.out.println("[Server] Both players bet — dealing cards!");
                            engine.dealInitialCards();
                        }
                    } catch (Exception e) {
                        System.err.println("[Server] Bet failed for player " + playerId
                                + " (amount=" + message.getBetAmount() + "): " + e.getMessage());
                    }
                    broadcastStateToAll();
                }
            }
            case PLAYER_ACTION -> {
                Player player = getPlayer(playerId);
                if (player != null) {
                    try {
                        switch (message.getAction()) {
                            case HIT -> {
                                if (player.hasSplit()) {
                                    engine.playerHitDuringSplit(player);
                                } else {
                                    engine.playerHit(player);
                                }
                            }
                            case STAND -> {
                                if (!player.hasSplit()) {
                                    engine.playerStand(player);
                                }
                            }
                            case DOUBLE_DOWN -> engine.playerDoubleDown(player);
                            case SPLIT -> engine.playerSplit(player);
                            case HIT_SPLIT_HAND -> {
                                boolean busted = engine.playerHitSplitHand(player);
                                if (busted) {
                                    engine.getTurnManager().nextTurn();
                                }
                            }
                            case STAND_SPLIT_HAND -> engine.getTurnManager().nextTurn();
                        }

                        if (engine.getGameState().getPhase() == GamePhase.SHOWDOWN) {
                            engine.resolveShowdown();
                            broadcastMessage(GameMessage.showdown(engine.getGameState()));
                        }

                        broadcastStateToAll();
                    } catch (Exception e) {

                    }
                }
            }
            case CHAT -> {

                broadcastMessage(message);
            }
            case DEAL_REQUEST -> {

                engine.startNewRound();
                broadcastStateToAll();
            }
            case HEARTBEAT -> {

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

    private boolean bothPlayersBet() {
        Player p1 = engine.getGameState().getPlayer1();
        Player p2 = engine.getGameState().getPlayer2();
        return p1 != null && p2 != null
                && p1.getCurrentBet() > 0 && p2.getCurrentBet() > 0;
    }

    private Player getPlayer(int playerId) {
        if (playerId == 1) return engine.getGameState().getPlayer1();
        if (playerId == 2) return engine.getGameState().getPlayer2();
        return null;
    }

    private void broadcastStateToAll() {
        for (ClientHandler client : clients) {
            GameState filtered = engine.getVisibleStateForPlayer(client.playerId);
            client.sendMessage(GameMessage.stateUpdate(filtered));
        }
    }

    private void broadcastMessage(GameMessage message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

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

    public void setListener(GameServerListener listener) {
        this.listener = listener;
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;
        private volatile boolean connected = false;

        ClientHandler(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;

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

            } catch (IOException | ClassNotFoundException e) {

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

    public interface GameServerListener {
        void onPlayerDisconnected(int playerId);
    }
}
