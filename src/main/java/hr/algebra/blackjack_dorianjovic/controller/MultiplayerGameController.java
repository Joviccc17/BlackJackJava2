package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.network.*;
import hr.algebra.blackjack_dorianjovic.threading.AppExecutorService;
import hr.algebra.blackjack_dorianjovic.view.HandView;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the multiplayer game view (mp-game-view.fxml).
 * Receives game state updates from the server via GameClient,
 * sends player actions, and handles RMI chat.
 */
public class MultiplayerGameController {

    // --- FXML Components ---
    @FXML private Label lblOpponentName;
    @FXML private HBox opponentCardArea;
    @FXML private Label lblOpponentScore;

    @FXML private Label lblStatus;
    @FXML private Label lblPot;

    @FXML private HBox bettingArea;
    @FXML private TextField txtBetAmount;

    @FXML private Label lblPlayerName;
    @FXML private HBox playerCardArea;
    @FXML private Label lblPlayerScore;

    @FXML private HBox actionButtons;
    @FXML private Button btnHit;
    @FXML private Button btnStand;
    @FXML private Button btnSplit;

    @FXML private HBox roundOverButtons;

    @FXML private TextArea txtChatLog;
    @FXML private TextField txtChatInput;

    @FXML private Label lblChips;
    @FXML private Label lblCurrentBet;
    @FXML private Label lblRound;

    // --- State ---
    private GameClient client;
    private ChatService chatService;
    private GameServer gameServer; // null if this instance is the joiner
    private int localPlayerId;
    private GameState currentState;
    private HandView playerHandView;
    private HandView opponentHandView;
    private ScheduledFuture<?> chatPollFuture;
    private int lastChatIndex = 0;

    @FXML
    public void initialize() {
        playerHandView = new HandView();
        opponentHandView = new HandView();
        playerCardArea.getChildren().add(playerHandView);
        opponentCardArea.getChildren().add(opponentHandView);
    }

    /**
     * Called by LobbyController to set up the multiplayer game.
     */
    public void initMultiplayer(GameClient client, ChatService chatService,
                                int localPlayerId, GameServer gameServer) {
        this.client = client;
        this.chatService = chatService;
        this.localPlayerId = localPlayerId;
        this.gameServer = gameServer;

        lblPlayerName.setText("Player " + localPlayerId + " (You)");
        lblOpponentName.setText("Player " + (localPlayerId == 1 ? 2 : 1));

        // Listen for incoming messages from the server
        client.setOnMessageReceived(message -> Platform.runLater(() -> handleServerMessage(message)));

        // Start chat polling
        startChatPolling();

        // Request current state from server (in case we missed the initial STATE_UPDATE)
        client.requestStateRefresh();

        lblStatus.setText("Place your bet to start!");
    }

    /**
     * Handles messages received from the GameServer.
     */
    private void handleServerMessage(GameMessage message) {
        switch (message.getType()) {
            case STATE_UPDATE -> {
                currentState = message.getGameState();
                updateUI();
            }
            case SHOWDOWN -> {
                currentState = message.getGameState();
                updateUI();
                showShowdownResult();
            }
            case PLAYER_LEFT -> {
                lblStatus.setText(message.getSenderName() + " disconnected!");
                actionButtons.setVisible(false);
                bettingArea.setVisible(false);
                roundOverButtons.setVisible(true);
            }
            case CHAT -> {
                // Chat handled by RMI polling
            }
        }
    }

    // ========================================================================
    // FXML Action Handlers
    // ========================================================================

    @FXML
    private void onPlaceBet() {
        try {
            int bet = Integer.parseInt(txtBetAmount.getText().trim());
            client.sendBet(bet);
            lblStatus.setText("Bet placed! Waiting for opponent...");
            bettingArea.setVisible(false);
        } catch (NumberFormatException e) {
            lblStatus.setText("Please enter a valid bet amount!");
        }
    }

    @FXML
    private void onHit() {
        client.sendAction(PlayerAction.HIT);
    }

    @FXML
    private void onStand() {
        client.sendAction(PlayerAction.STAND);
    }

    @FXML
    private void onSplit() {
        client.sendAction(PlayerAction.SPLIT);
    }

    @FXML
    private void onNewRound() {
        client.sendDealRequest();
        roundOverButtons.setVisible(false);
        bettingArea.setVisible(true);
        lblStatus.setText("Place your bet for the next round!");
    }



    @FXML
    private void onLeaveGame() {
        stopChatPolling();
        if (client != null) client.disconnect();
        if (gameServer != null) gameServer.stop();

        try {
            SceneManager.getInstance().switchScene("main-menu-view.fxml");
        } catch (IOException e) {
            lblStatus.setText("Error returning to menu.");
        }
    }

    // ========================================================================
    // Chat (RMI)
    // ========================================================================

    @FXML
    private void onSendChat() {
        String text = txtChatInput.getText().trim();
        if (text.isEmpty() || chatService == null) return;

        String senderName = "Player " + localPlayerId;
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

    // ========================================================================
    // UI Updates
    // ========================================================================

    private void updateUI() {
        if (currentState == null) return;

        updateCards();
        updateScores();
        updateInfoBar();
        updatePhaseUI();
    }

    private void updateCards() {
        Player localPlayer = getLocalPlayer();
        Player opponent = getOpponent();

        if (localPlayer != null) {
            playerHandView.updateCards(localPlayer.getHand().getCards());
        }
        if (opponent != null) {
            opponentHandView.updateCards(opponent.getHand().getCards());
        }
    }

    private void updateScores() {
        Player localPlayer = getLocalPlayer();
        Player opponent = getOpponent();

        if (localPlayer != null) {
            lblPlayerScore.setText("Score: " + localPlayer.getHand().calculateScore());
        }

        if (opponent != null) {
            GamePhase phase = currentState.getPhase();
            if (phase == GamePhase.SHOWDOWN || phase == GamePhase.ROUND_OVER) {
                lblOpponentScore.setText("Score: " + opponent.getHand().calculateScore());
            } else {
                // Show visible card value + ? for hidden cards
                int visibleScore = opponent.getHand().getCards().stream()
                        .filter(Card::isFaceUp)
                        .mapToInt(Card::getValue)
                        .sum();
                lblOpponentScore.setText("Score: " + visibleScore + " + ?");
            }
        }
    }

    private void updateInfoBar() {
        Player localPlayer = getLocalPlayer();
        if (localPlayer != null) {
            lblChips.setText("Chips: " + localPlayer.getChips());
            lblCurrentBet.setText("Bet: " + localPlayer.getCurrentBet());
        }
        lblRound.setText("Round: " + currentState.getRoundNumber());
        lblPot.setText("Pot: " + currentState.getPot());
    }

    private void updatePhaseUI() {
        GamePhase phase = currentState.getPhase();

        switch (phase) {
            case BETTING -> {
                bettingArea.setVisible(true);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Place your bet!");
            }
            case DEALING -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Dealing cards...");
            }
            case PLAYER_TURN -> {
                bettingArea.setVisible(false);
                if (localPlayerId == 1) {
                    actionButtons.setVisible(true);
                    lblStatus.setText("Your turn — Hit or Stand?");
                    updateActionButtonStates();
                } else {
                    actionButtons.setVisible(false);
                    lblStatus.setText("Waiting for Player 1...");
                }
                roundOverButtons.setVisible(false);
            }
            case PLAYER2_TURN -> {
                bettingArea.setVisible(false);
                if (localPlayerId == 2) {
                    actionButtons.setVisible(true);
                    lblStatus.setText("Your turn — Hit or Stand?");
                    updateActionButtonStates();
                } else {
                    actionButtons.setVisible(false);
                    lblStatus.setText("Waiting for Player 2...");
                }
                roundOverButtons.setVisible(false);
            }
            case SHOWDOWN, ROUND_OVER -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(true);
                String msg = currentState.getResultMessage();
                lblStatus.setText(msg != null ? msg : "Round over!");
            }
            default -> {
                // WAITING, DEALER_TURN — not used in multiplayer
            }
        }
    }

    private void updateActionButtonStates() {
        Player localPlayer = getLocalPlayer();
        if (localPlayer != null) {
            btnSplit.setDisable(!localPlayer.getHand().canSplit()
                    || localPlayer.getChips() < localPlayer.getCurrentBet());
        }
    }

    private void showShowdownResult() {
        if (currentState == null || currentState.getResultMessage() == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Result");
        alert.setHeaderText("Round Over!");
        alert.setContentText(currentState.getResultMessage());
        alert.show();
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private Player getLocalPlayer() {
        if (currentState == null) return null;
        return localPlayerId == 1 ? currentState.getPlayer1() : currentState.getPlayer2();
    }

    private Player getOpponent() {
        if (currentState == null) return null;
        return localPlayerId == 1 ? currentState.getPlayer2() : currentState.getPlayer1();
    }
}

