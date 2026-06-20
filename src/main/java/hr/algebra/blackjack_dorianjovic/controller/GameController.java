package hr.algebra.blackjack_dorianjovic.controller;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.config.XmlConfigReader;
import hr.algebra.blackjack_dorianjovic.engine.GameEngine;
import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.serialization.SaveManager;
import hr.algebra.blackjack_dorianjovic.threading.AppExecutorService;
import hr.algebra.blackjack_dorianjovic.threading.DealerPlayTask;
import hr.algebra.blackjack_dorianjovic.threading.GameSaveTask;
import hr.algebra.blackjack_dorianjovic.view.HandView;
import hr.algebra.blackjack_dorianjovic.view.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class GameController {

    @FXML private Label lblOpponentName;
    @FXML private HBox opponentCardArea;
    @FXML private Label lblOpponentScore;

    @FXML private Label lblStatus;
    @FXML private Label lblPot;

    @FXML private HBox bettingArea;
    @FXML private TextField txtBetAmount;
    @FXML private Button btnPlaceBet;

    @FXML private Label lblPlayerName;
    @FXML private HBox playerCardArea;
    @FXML private Label lblPlayerScore;

    @FXML private VBox splitHandArea;
    @FXML private HBox splitCardArea;
    @FXML private Label lblSplitScore;
    @FXML private Label lblHand1Label;
    @FXML private Label lblHand2Label;

    @FXML private HBox actionButtons;
    @FXML private Button btnHit;
    @FXML private Button btnStand;
    @FXML private Button btnDoubleDown;
    @FXML private Button btnSplit;

    @FXML private HBox roundOverButtons;
    @FXML private Button btnNewRound;
    @FXML private Button btnSaveGame;
    @FXML private Button btnBackToMenu;

    @FXML private Label lblChips;
    @FXML private Label lblCurrentBet;
    @FXML private Label lblRound;

    private GameEngine engine;
    private GameMode gameMode;
    private HandView playerHandView;
    private HandView splitHandView;
    private HandView opponentHandView;
    private boolean playingSplitHand = false;

    @FXML
    public void initialize() {
        playerHandView = new HandView();
        splitHandView = new HandView();
        opponentHandView = new HandView();
        playerCardArea.getChildren().add(playerHandView);
        splitCardArea.getChildren().add(splitHandView);
        opponentCardArea.getChildren().add(opponentHandView);
    }

    public void initSinglePlayer() {
        this.gameMode = GameMode.SINGLE_PLAYER;
        GameConfig config = loadConfig();
        engine = new GameEngine(config, GameMode.SINGLE_PLAYER);

        lblOpponentName.setText("Dealer");
        lblPlayerName.setText("Player");
        lblPot.setVisible(false);
        btnDoubleDown.setVisible(true);

        bindPlayerProperties();
        engine.startNewRound();
        updateUI();
    }

    public void initFromSavedState(GameState savedState) {
        GameConfig config = loadConfig();
        this.gameMode = savedState.getMode();
        this.engine = new GameEngine(config, savedState);

        if (gameMode == GameMode.SINGLE_PLAYER) {
            lblOpponentName.setText("Dealer");
            lblPlayerName.setText("Player");
            lblPot.setVisible(false);
        } else {
            lblOpponentName.setText("Opponent");
            lblPlayerName.setText("Player 1");
            lblPot.setVisible(true);
        }

        bindPlayerProperties();
        updateUI();
    }

    @FXML
    private void onPlaceBet() {
        try {
            int bet = Integer.parseInt(txtBetAmount.getText().trim());
            Player player = engine.getGameState().getPlayer1();
            engine.placeBet(player, bet);

            if (gameMode == GameMode.SINGLE_PLAYER) {

                engine.dealInitialCards();

                if (engine.getGameState().getPhase() == GamePhase.SHOWDOWN) {
                    int betBefore = player.getCurrentBet();
                    int chipsBefore = player.getChips();
                    GameResult result = engine.resolveShowdown();
                    updateUI();

                    PauseTransition delay = new PauseTransition(Duration.seconds(1));
                    delay.setOnFinished(e -> showRoundResultPopup(result, betBefore, chipsBefore));
                    delay.play();
                    return;
                }
            }

            updateUI();
        } catch (NumberFormatException e) {
            lblStatus.setText("Please enter a valid number!");
        } catch (IllegalArgumentException e) {
            lblStatus.setText(e.getMessage());
        }
    }

    @FXML
    private void onHit() {
        Player currentPlayer = getCurrentLocalPlayer();
        if (currentPlayer == null) return;

        if (currentPlayer.hasSplit()) {
            if (!playingSplitHand) {

                boolean busted = engine.playerHitDuringSplit(currentPlayer);
                updateUI();
                if (busted) switchToSplitHand2();
            } else {

                boolean busted = engine.playerHitSplitHand(currentPlayer);
                updateUI();
                if (busted) finishSplitTurn(currentPlayer);
            }
        } else {
            boolean busted = engine.playerHit(currentPlayer);
            updateUI();
            if (busted) {
                handlePostPlayerTurn();
            }
        }
    }

    @FXML
    private void onStand() {
        Player currentPlayer = getCurrentLocalPlayer();
        if (currentPlayer == null) return;

        if (currentPlayer.hasSplit()) {
            if (!playingSplitHand) {

                switchToSplitHand2();
            } else {

                finishSplitTurn(currentPlayer);
            }
        } else {
            engine.playerStand(currentPlayer);
            updateUI();
            handlePostPlayerTurn();
        }
    }

    private void switchToSplitHand2() {
        playingSplitHand = true;
        lblHand1Label.setText("  Hand 1");
        lblHand2Label.setText("▶ Hand 2");
        updateUI();
        lblStatus.setText("Now playing Hand 2 — Hit or Stand?");
    }

    private void finishSplitTurn(Player player) {
        playingSplitHand = false;
        splitHandArea.setVisible(false);

        engine.getTurnManager().nextTurn();
        updateUI();
        handlePostPlayerTurn();
    }

    @FXML
    private void onDoubleDown() {
        Player currentPlayer = getCurrentLocalPlayer();
        if (currentPlayer == null) return;

        try {
            engine.playerDoubleDown(currentPlayer);
            updateUI();
            handlePostPlayerTurn();
        } catch (IllegalStateException e) {
            lblStatus.setText(e.getMessage());
        }
    }

    @FXML
    private void onSplit() {
        Player currentPlayer = getCurrentLocalPlayer();
        if (currentPlayer == null) return;

        try {
            engine.playerSplit(currentPlayer);
            playingSplitHand = false;
            splitHandArea.setVisible(true);
            lblHand1Label.setText("▶ Hand 1");
            lblHand2Label.setText("  Hand 2");
            updateUI();
            lblStatus.setText("Split! Playing Hand 1 — Hit or Stand?");
        } catch (IllegalStateException e) {
            lblStatus.setText(e.getMessage());
        }
    }

    @FXML
    private void onNewRound() {
        playingSplitHand = false;
        splitHandArea.setVisible(false);
        engine.startNewRound();
        updateUI();
    }

    @FXML
    private void onSaveGame() {
        if (gameMode == GameMode.MULTIPLAYER) {
            lblStatus.setText("Cannot save multiplayer games.");
            return;
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(1, 1, 2, 3, 4, 5);
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Choose a save slot");
        dialog.setContentText("Slot:");

        dialog.showAndWait().ifPresent(slot -> {
            SaveManager saveManager = new SaveManager();
            GameSaveTask saveTask = new GameSaveTask(engine.getGameState(), slot, saveManager);

            saveTask.setOnSucceeded(event ->
                    Platform.runLater(() -> lblStatus.setText("Game saved to slot " + slot + "!"))
            );
            saveTask.setOnFailed(event ->
                    Platform.runLater(() -> lblStatus.setText("Failed to save game."))
            );

            lblStatus.setText("Saving...");
            AppExecutorService.getInstance().submit(saveTask);
        });
    }

    @FXML
    private void onBackToMenu() {
        try {
            SceneManager.getInstance().switchScene("main-menu-view.fxml");
        } catch (IOException e) {
            lblStatus.setText("Error returning to menu");
        }
    }

    private void handlePostPlayerTurn() {
        GamePhase phase = engine.getGameState().getPhase();

        if (gameMode == GameMode.SINGLE_PLAYER) {
            int betBefore = engine.getGameState().getPlayer1().getCurrentBet();
            int chipsBefore = engine.getGameState().getPlayer1().getChips();

            if (phase == GamePhase.DEALER_TURN) {

                actionButtons.setVisible(false);
                lblStatus.setText("Dealer's turn...");

                DealerPlayTask dealerTask = new DealerPlayTask(engine, statusMsg -> {
                    Platform.runLater(this::updateUI);
                }, 1200);

                dealerTask.messageProperty().addListener((obs, oldMsg, newMsg) ->
                    Platform.runLater(() -> lblStatus.setText(newMsg))
                );

                dealerTask.setOnSucceeded(event -> Platform.runLater(() -> {

                    engine.getTurnManager().nextTurn();
                    updateUI();

                    if (engine.getGameState().getPhase() == GamePhase.SHOWDOWN) {
                        GameResult result = engine.resolveShowdown();
                        updateUI();

                        PauseTransition delay = new PauseTransition(Duration.seconds(1));
                        delay.setOnFinished(e -> showRoundResultPopup(result, betBefore, chipsBefore));
                        delay.play();
                    }
                }));

                dealerTask.setOnFailed(event ->
                    Platform.runLater(() -> lblStatus.setText("Error during dealer turn!"))
                );

                AppExecutorService.getInstance().submit(dealerTask);

            } else if (phase == GamePhase.SHOWDOWN) {

                GameResult result = engine.resolveShowdown();
                updateUI();

                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(e -> showRoundResultPopup(result, betBefore, chipsBefore));
                delay.play();
            }
        } else {

            if (phase == GamePhase.SHOWDOWN) {
                engine.resolveShowdown();
                updateUI();
            }

        }
    }

    private void updateUI() {
        GameState state = engine.getGameState();
        GamePhase phase = state.getPhase();

        updateCards(state);
        updateScores(state);
        updateInfoBar(state);
        updatePhaseUI(phase, state);
    }

    private void updateCards(GameState state) {
        Player player = state.getPlayer1();
        playerHandView.updateCards(player.getHand().getCards());

        if (player.hasSplit()) {
            splitHandView.updateCards(player.getSplitHand().getCards());
            splitHandArea.setVisible(true);
            lblSplitScore.setText("Score: " + player.getSplitHand().calculateScore());
        } else {
            splitHandArea.setVisible(false);
        }

        if (gameMode == GameMode.SINGLE_PLAYER && state.getDealer() != null) {
            opponentHandView.updateCards(state.getDealer().getHand().getCards());
        } else if (state.getPlayer2() != null) {
            opponentHandView.updateCards(state.getPlayer2().getHand().getCards());
        }
    }

    private void updateScores(GameState state) {
        Player player = state.getPlayer1();
        lblPlayerScore.setText("Score: " + player.getHand().calculateScore());

        if (gameMode == GameMode.SINGLE_PLAYER && state.getDealer() != null) {
            Dealer dealer = state.getDealer();

            boolean allRevealed = dealer.getHand().getCards().stream().allMatch(Card::isFaceUp);
            if (allRevealed) {
                lblOpponentScore.setText("Score: " + dealer.getHand().calculateScore());
            } else {

                int visibleScore = dealer.getHand().getCards().stream()
                        .filter(Card::isFaceUp)
                        .mapToInt(Card::getValue)
                        .sum();
                lblOpponentScore.setText("Score: " + visibleScore + " + ?");
            }
        } else if (state.getPlayer2() != null) {

            GamePhase phase = state.getPhase();
            if (phase == GamePhase.SHOWDOWN || phase == GamePhase.ROUND_OVER) {
                lblOpponentScore.setText("Score: " + state.getPlayer2().getHand().calculateScore());
            } else {
                lblOpponentScore.setText("Score: ???");
            }
        } else {
            lblOpponentScore.setText("Score: ?");
        }
    }

    private void updateInfoBar(GameState state) {
        lblRound.setText("Round: " + state.getRoundNumber());

        if (gameMode == GameMode.MULTIPLAYER) {
            lblPot.setText("Pot: " + state.getPot());
        }
    }

    private void updatePhaseUI(GamePhase phase, GameState state) {
        switch (phase) {
            case WAITING, BETTING -> {
                bettingArea.setVisible(true);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Place your bet (min: " + engine.getConfig().getMinBet()
                        + ", max: " + engine.getConfig().getMaxBet() + ")");
                txtBetAmount.setText(String.valueOf(engine.getConfig().getMinBet()));
            }
            case DEALING, PLAYER_TURN -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(true);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Your turn — Hit or Stand?");

                Player player = state.getPlayer1();
                btnDoubleDown.setDisable(player.getHand().size() != 2
                        || player.getChips() < player.getCurrentBet());
                btnSplit.setDisable(!player.getHand().canSplit()
                        || player.getChips() < player.getCurrentBet());
            }
            case PLAYER2_TURN -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Waiting for opponent...");
            }
            case DEALER_TURN -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(false);
                lblStatus.setText("Dealer's turn...");
            }
            case SHOWDOWN, ROUND_OVER -> {
                bettingArea.setVisible(false);
                actionButtons.setVisible(false);
                roundOverButtons.setVisible(true);
                String msg = state.getResultMessage();
                lblStatus.setText(msg != null ? msg : "Round over!");
            }
        }
    }

    private Player getCurrentLocalPlayer() {
        if (gameMode == GameMode.SINGLE_PLAYER) {
            return engine.getGameState().getPlayer1();
        }

        return engine.getTurnManager().getCurrentPlayer();
    }

    private void showRoundResultPopup(GameResult result, int betAmount, int chipsBefore) {
        int chipsAfter = engine.getGameState().getPlayer1().getChips();
        int netChange = chipsAfter - chipsBefore;

        String title;
        String header;
        String content;
        Alert.AlertType alertType;

        switch (result) {
            case BLACKJACK -> {
                alertType = Alert.AlertType.INFORMATION;
                title = "Blackjack!";
                header = "🎉 BLACKJACK! You win!";
                content = "You got a natural 21!\n"
                        + "Bet: " + betAmount + " chips\n"
                        + "Winnings: +" + netChange + " chips\n"
                        + "Total chips: " + chipsAfter;
            }
            case PLAYER_WINS -> {
                alertType = Alert.AlertType.INFORMATION;
                title = "You Win!";
                header = "🏆 Congratulations! You won the round!";
                int playerScore = engine.getGameState().getPlayer1().getHand().calculateScore();
                int dealerScore = engine.getGameState().getDealer().getHand().calculateScore();
                content = "Your score: " + playerScore + " vs Dealer: " + dealerScore + "\n"
                        + "Bet: " + betAmount + " chips\n"
                        + "Winnings: +" + netChange + " chips\n"
                        + "Total chips: " + chipsAfter;
            }
            case DEALER_WINS -> {
                alertType = Alert.AlertType.WARNING;
                title = "Dealer Wins";
                header = "😔 The dealer won this round.";
                int playerScore = engine.getGameState().getPlayer1().getHand().calculateScore();
                int dealerScore = engine.getGameState().getDealer().getHand().calculateScore();
                String bustText = engine.getGameState().getPlayer1().getHand().isBusted()
                        ? "You busted!\n" : "";
                content = bustText
                        + "Your score: " + playerScore + " vs Dealer: " + dealerScore + "\n"
                        + "Lost: -" + betAmount + " chips\n"
                        + "Total chips: " + chipsAfter;
            }
            case PUSH -> {
                alertType = Alert.AlertType.INFORMATION;
                title = "Push!";
                header = "🤝 It's a tie!";
                int playerScore = engine.getGameState().getPlayer1().getHand().calculateScore();
                content = "Both scored " + playerScore + "\n"
                        + "Your bet of " + betAmount + " chips has been returned.\n"
                        + "Total chips: " + chipsAfter;
            }
            default -> {
                alertType = Alert.AlertType.INFORMATION;
                title = "Round Over";
                header = "Round complete.";
                content = "Total chips: " + chipsAfter;
            }
        }

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }

    private void bindPlayerProperties() {
        Player p = engine.getGameState().getPlayer1();
        lblChips.textProperty().bind(
                Bindings.concat("Chips: ", p.chipsProperty().asString()));
        lblCurrentBet.textProperty().bind(
                Bindings.concat("Bet: ", p.currentBetProperty().asString()));
        playerHandView.bindToObservableList(p.getHand().observableCards());
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
        } catch (Exception e) {
            System.err.println("Failed to load config, using defaults: " + e.getMessage());
        }
        return new GameConfig();
    }
}
