package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.config.XmlConfigReader;
import hr.algebra.blackjack_dorianjovic.network.*;
import hr.algebra.blackjack_dorianjovic.threading.AppExecutorService;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the multiplayer lobby.
 * Supports two modes:
 *   HOST — starts GameServer, RMI registry, waits for a player to connect
 *   JOIN — connects to a host via RMI matchmaking, then TCP
 */
public class LobbyController {

    @FXML private Label lblTitle;
    @FXML private Label lblStatus;
    @FXML private HBox connectionArea;
    @FXML private TextField txtHost;
    @FXML private TextField txtPort;
    @FXML private Button btnConnect;
    @FXML private ListView<String> lstPlayers;
    @FXML private TextArea txtChatLog;
    @FXML private TextField txtChatInput;
    @FXML private Button btnStartGame;

    private boolean isHost;
    private GameServer gameServer;
    private GameClient gameClient;
    private ChatService chatService;
    private MatchmakingService matchmakingService;
    private Registry rmiRegistry;
    private ScheduledFuture<?> chatPollFuture;
    private int lastChatIndex = 0;

    /**
     * Sets up the lobby as a HOST (starts server + RMI services).
     */
    public void initAsHost() {
        isHost = true;
        lblTitle.setText("Host Multiplayer");
        connectionArea.setVisible(false);
        btnStartGame.setVisible(false);

        GameConfig config = loadConfig();
        int port = config.getServerPort();

        lblStatus.setText("Starting server on port " + port + "...");

        AppExecutorService.getInstance().submit(() -> {
            try {
                // Start RMI registry and bind services
                rmiRegistry = RmiServiceManager.startRegistry(RmiServiceManager.RMI_REGISTRY_PORT);
                ChatServiceImpl chatImpl = new ChatServiceImpl();
                MatchmakingServiceImpl matchImpl = new MatchmakingServiceImpl(port);
                RmiServiceManager.bindChatService(rmiRegistry, chatImpl);
                RmiServiceManager.bindMatchmakingService(rmiRegistry, matchImpl);

                chatService = chatImpl;

                Platform.runLater(() -> {
                    lblStatus.setText("Server started! Waiting for opponent to connect...\n"
                            + "Port: " + port + " | Share your IP with your opponent.");
                    lstPlayers.getItems().add("Player 1 (You - Host)");
                    startChatPolling();
                });

                // Create TCP game server
                gameServer = new GameServer(port, config);
                gameServer.setListener(playerId ->
                        Platform.runLater(() -> {
                            lblStatus.setText("Player " + playerId + " disconnected!");
                            lstPlayers.getItems().removeIf(s -> s.contains("Player " + playerId));
                        })
                );

                // Start server with onReady callback:
                // As soon as the socket is open, connect the host as TCP client #1
                gameServer.start(() -> {
                    // This runs on the server thread, right after ServerSocket is open
                    // Connect host as client on a separate thread
                    AppExecutorService.getInstance().submit(() -> {
                        try {
                            gameClient = new GameClient("localhost", port, "Player 1");
                            gameClient.setPlayerId(1);

                            gameClient.setOnMessageReceived(message -> Platform.runLater(() -> {
                                switch (message.getType()) {
                                    case GAME_START -> {
                                        lblStatus.setText("Opponent connected! Game starting...");
                                        matchImpl.setGameStarted(true);
                                        transitionToGame();
                                    }
                                    case PLAYER_JOINED -> {
                                        lstPlayers.getItems().add(message.getSenderName() + " joined");
                                        lblStatus.setText(message.getSenderName() + " connected!");
                                    }
                                    case PLAYER_LEFT -> {
                                        lstPlayers.getItems().removeIf(s ->
                                                s.contains(message.getSenderName()));
                                        lblStatus.setText(message.getSenderName() + " left!");
                                    }
                                    default -> {}
                                }
                            }));

                            gameClient.connect();
                            Platform.runLater(() ->
                                    lblStatus.setText("Server running. Waiting for opponent...\nPort: " + port));
                        } catch (Exception e) {
                            Platform.runLater(() ->
                                    lblStatus.setText("Host client error: " + e.getMessage()));
                        }
                    });
                });
                // gameServer.start() blocks here until both clients connect

            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Server error: " + e.getMessage()));
            }
        });
    }

    /**
     * Sets up the lobby as a JOINER (connects to a remote host).
     */
    public void initAsJoiner() {
        isHost = false;
        lblTitle.setText("Join Multiplayer");
        connectionArea.setVisible(true);
        btnStartGame.setVisible(false);

        GameConfig config = loadConfig();
        txtHost.setText(config.getServerHost());
        txtPort.setText(String.valueOf(config.getServerPort()));

        lblStatus.setText("Enter host address and click Connect.");
    }

    /**
     * Connect button handler (joiner only).
     */
    @FXML
    private void onConnect() {
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("Invalid port number.");
            return;
        }

        lblStatus.setText("Connecting to " + host + ":" + port + "...");
        btnConnect.setDisable(true);

        AppExecutorService.getInstance().submit(() -> {
            try {
                // Look up RMI services via JNDI
                matchmakingService = RmiServiceManager.lookupMatchmakingService(host, RmiServiceManager.RMI_REGISTRY_PORT);
                chatService = RmiServiceManager.lookupChatService(host, RmiServiceManager.RMI_REGISTRY_PORT);

                matchmakingService.registerPlayer("Player 2");
                int gamePort = matchmakingService.getGameServerPort();

                // Joiner is ALWAYS player 2 (host is player 1 by TCP connection order)
                int playerId = 2;

                Platform.runLater(() -> {
                    lblStatus.setText("Registered as Player " + playerId + ". Connecting to game...");
                    startChatPolling();
                    refreshPlayerList();
                });

                // Connect as TCP client directly on this background thread
                gameClient = new GameClient(host, gamePort, "Player " + playerId);
                gameClient.setPlayerId(playerId);

                gameClient.setOnMessageReceived(message -> Platform.runLater(() -> {
                    switch (message.getType()) {
                        case GAME_START -> transitionToGame();
                        case PLAYER_JOINED -> {
                            lstPlayers.getItems().add(message.getSenderName() + " joined");
                            lblStatus.setText(message.getSenderName() + " joined!");
                        }
                        case PLAYER_LEFT -> {
                            lstPlayers.getItems().removeIf(s -> s.contains(message.getSenderName()));
                            lblStatus.setText(message.getSenderName() + " left!");
                        }
                        default -> {}
                    }
                }));

                gameClient.connect();
                Platform.runLater(() -> lblStatus.setText("Connected! Waiting for game to start..."));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Connection failed: " + e.getMessage());
                    btnConnect.setDisable(false);
                });
            }
        });
    }

    /**
     * Connects this instance as a TCP game client.
     */
    private void connectAsClient(String host, int port, int playerId) {
        AppExecutorService.getInstance().submit(() -> {
            try {
                gameClient = new GameClient(host, port, "Player " + playerId);
                gameClient.setPlayerId(playerId);

                gameClient.setOnMessageReceived(message -> Platform.runLater(() -> {
                    switch (message.getType()) {
                        case GAME_START -> transitionToGame();
                        case PLAYER_JOINED -> {
                            lstPlayers.getItems().add(message.getSenderName() + " joined");
                            lblStatus.setText(message.getSenderName() + " joined!");
                        }
                        case PLAYER_LEFT -> {
                            lstPlayers.getItems().removeIf(s -> s.contains(message.getSenderName()));
                            lblStatus.setText(message.getSenderName() + " left!");
                        }
                        case STATE_UPDATE, SHOWDOWN -> {
                            // Game has started — handled by GameController
                        }
                    }
                }));

                gameClient.connect();
                Platform.runLater(() -> lblStatus.setText("Connected! Waiting for game to start..."));

            } catch (IOException e) {
                Platform.runLater(() -> lblStatus.setText("TCP connection failed: " + e.getMessage()));
            }
        });
    }

    /**
     * Transitions from lobby to the multiplayer game view.
     */
    private void transitionToGame() {
        stopChatPolling();
        try {
            MultiplayerGameController controller = SceneManager.getInstance()
                    .switchSceneAndGetController("mp-game-view.fxml");
            controller.initMultiplayer(gameClient, chatService,
                    gameClient.getPlayerId(), gameServer);
        } catch (IOException e) {
            lblStatus.setText("Failed to start game: " + e.getMessage());
        }
    }

    /**
     * Refreshes the player list from the matchmaking service.
     */
    private void refreshPlayerList() {
        if (matchmakingService == null) return;
        try {
            List<String> players = matchmakingService.getWaitingPlayers();
            lstPlayers.getItems().clear();
            for (int i = 0; i < players.size(); i++) {
                lstPlayers.getItems().add("Player " + (i + 1) + ": " + players.get(i));
            }
        } catch (Exception e) {
            lblStatus.setText("Error fetching players.");
        }
    }

    // ========================================================================
    // Chat
    // ========================================================================

    @FXML
    private void onSendChat() {
        String text = txtChatInput.getText().trim();
        if (text.isEmpty() || chatService == null) return;

        String senderName = isHost ? "Player 1" : "Player 2";
        try {
            chatService.sendMessage(senderName, text);
            txtChatInput.clear();
        } catch (Exception e) {
            txtChatLog.appendText("[Error] Failed to send message.\n");
        }
    }

    /**
     * Polls RMI chat service every 500ms for new messages.
     */
    private void startChatPolling() {
        chatPollFuture = AppExecutorService.getInstance().getScheduledPool()
                .scheduleAtFixedRate(() -> {
                    try {
                        if (chatService == null) return;
                        List<String> newMsgs = chatService.getMessages(lastChatIndex);
                        if (!newMsgs.isEmpty()) {
                            lastChatIndex += newMsgs.size();
                            Platform.runLater(() -> {
                                for (String msg : newMsgs) {
                                    txtChatLog.appendText(msg + "\n");
                                }
                            });
                        }
                    } catch (Exception ignored) {}
                }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void stopChatPolling() {
        if (chatPollFuture != null) {
            chatPollFuture.cancel(false);
        }
    }

    // ========================================================================
    // Navigation
    // ========================================================================

    @FXML
    private void onStartGame() {
        // Manual start if needed
    }

    @FXML
    private void onBack() {
        stopChatPolling();
        if (gameClient != null) gameClient.disconnect();
        if (gameServer != null) gameServer.stop();

        try {
            SceneManager.getInstance().switchScene("main-menu-view.fxml");
        } catch (IOException e) {
            lblStatus.setText("Error returning to menu.");
        }
    }

    private static final String CONFIG_FILE_PATH = "config/game-config.xml";
    private static final String CONFIG_CLASSPATH =
            "/hr/algebra/blackjack_dorianjovic/config/game-config.xml";

    private GameConfig loadConfig() {
        XmlConfigReader reader = new XmlConfigReader();
        try {
            File configFile = new File(CONFIG_FILE_PATH);
            if (configFile.exists()) {
                return reader.readConfig(configFile);
            }
            return reader.readConfigFromClasspath(CONFIG_CLASSPATH);
        } catch (Exception ignored) {}
        return new GameConfig();
    }
}

