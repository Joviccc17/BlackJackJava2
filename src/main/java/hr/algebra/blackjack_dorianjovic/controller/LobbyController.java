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

                gameServer = new GameServer(port, config);
                gameServer.setListener(playerId ->
                        Platform.runLater(() -> {
                            lblStatus.setText("Player " + playerId + " disconnected!");
                            lstPlayers.getItems().removeIf(s -> s.contains("Player " + playerId));
                        })
                );

                gameServer.start(() -> {

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

            } catch (Exception e) {
                Platform.runLater(() -> lblStatus.setText("Server error: " + e.getMessage()));
            }
        });
    }

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

                matchmakingService = RmiServiceManager.lookupMatchmakingService(host, RmiServiceManager.RMI_REGISTRY_PORT);
                chatService = RmiServiceManager.lookupChatService(host, RmiServiceManager.RMI_REGISTRY_PORT);

                matchmakingService.registerPlayer("Player 2");
                int gamePort = matchmakingService.getGameServerPort();

                int playerId = 2;

                Platform.runLater(() -> {
                    lblStatus.setText("Registered as Player " + playerId + ". Connecting to game...");
                    startChatPolling();
                    refreshPlayerList();
                });

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

    @FXML
    private void onStartGame() {

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
