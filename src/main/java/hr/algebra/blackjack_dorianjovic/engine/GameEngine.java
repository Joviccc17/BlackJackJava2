package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.util.Documented;

/**
 * Central game orchestrator. Holds GameState and provides all game actions.
 * Mode-aware: checks gameState.getMode() to apply correct rules.
 *
 * SP flow:  startNewRound → placeBet → dealInitialCards → playerHit/Stand → executeDealerTurn → resolveShowdown
 * MP flow:  startNewRound → placeBet(both) → dealInitialCards → player1 Hit/Stand → player2 Hit/Stand → resolveShowdown
 */
public class GameEngine {

    private GameState gameState;
    private TurnManager turnManager;
    private final GameConfig config;

    public GameEngine(GameConfig config, GameMode mode) {
        this.config = config;
        initializeGame(mode);
    }

    /**
     * Initializes a fresh game with the given mode.
     */
    private void initializeGame(GameMode mode) {
        gameState = new GameState(mode);
        gameState.setDeck(new Deck(config.getNumberOfDecks()));

        if (mode == GameMode.SINGLE_PLAYER) {
            gameState.setPlayer1(new Player("Player", config.getStartingBalance()));
            gameState.setDealer(new Dealer(config.isDealerHitsSoft17()));
        } else {
            gameState.setPlayer1(new Player("Player 1", config.getStartingBalance()));
            gameState.setPlayer2(new Player("Player 2", config.getStartingBalance()));
        }

        turnManager = new TurnManager(gameState);
        gameState.setPhase(GamePhase.BETTING);
    }

    /**
     * Restores a game from a previously saved GameState (deserialization).
     */
    public GameEngine(GameConfig config, GameState savedState) {
        this.config = config;
        this.gameState = savedState;
        this.turnManager = new TurnManager(savedState);
    }

    // ========================================================================
    // ROUND LIFECYCLE
    // ========================================================================

    /**
     * Starts a new round: resets hands, increments round counter, goes to BETTING phase.
     */
    @Documented(description = "Starts a new round: resets hands, increments round counter, reshuffles if needed")
    public void startNewRound() {
        gameState.incrementRound();
        gameState.setResultMessage(null);
        gameState.setPot(0);

        gameState.getPlayer1().resetForNewRound();

        if (gameState.isSinglePlayer()) {
            gameState.getDealer().resetForNewRound();
        } else {
            gameState.getPlayer2().resetForNewRound();
        }

        // Reshuffle if less than 25% of cards remain
        if (gameState.getDeck().cardsRemaining() < gameState.getDeck().totalCards() / 4) {
            gameState.getDeck().shuffle();
        }

        gameState.setPhase(GamePhase.BETTING);
    }

    /**
     * Places a bet for the given player.
     * In SP: only player1 bets. In MP: both players bet (stakes go to pot).
     */
    public void placeBet(Player player, int amount) {
        if (amount < config.getMinBet() || amount > config.getMaxBet()) {
            throw new IllegalArgumentException(
                    "Bet must be between " + config.getMinBet() + " and " + config.getMaxBet());
        }
        player.placeBet(amount);

        if (gameState.isMultiplayer()) {
            gameState.addToPot(amount);
        }
    }

    /**
     * Deals initial cards after bets are placed.
     * SP: Player gets 2 face-up, Dealer gets 1 face-up + 1 face-down.
     * MP: Both players get 2 face-up (visibility is handled by network/GUI layer).
     */
    @Documented(description = "Deals initial cards: SP gives player 2 face-up, dealer 1 up + 1 down; MP gives both players 2 cards")
    public void dealInitialCards() {
        gameState.setPhase(GamePhase.DEALING);

        if (gameState.isSinglePlayer()) {
            // Player gets 2 face-up cards
            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer1(), true);

            // Dealer gets 1 face-up + 1 face-down (hole card)
            dealCardToPlayer(gameState.getDealer(), true);
            dealCardToPlayer(gameState.getDealer(), false);
        } else {
            // MP: both players get 2 face-up cards
            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer2(), true);
            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer2(), true);
        }

        // Start player turns
        turnManager.startTurns();

        // Auto-resolve natural blackjack (21 on initial 2 cards)
        if (gameState.isSinglePlayer()) {
            Player player = gameState.getPlayer1();
            Dealer dealer = gameState.getDealer();

            if (player.getHand().isBlackjack() || dealer.getHand().isBlackjack()) {
                // Reveal dealer's hole card
                for (Card c : dealer.getHand().getCards()) {
                    c.setFaceUp(true);
                }
                // Skip directly to showdown
                gameState.setPhase(GamePhase.SHOWDOWN);
            }
        }
    }

    private void dealCardToPlayer(Player player, boolean faceUp) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(faceUp);
        player.getHand().addCard(card);
    }

    // ========================================================================
    // PLAYER ACTIONS
    // ========================================================================

    /**
     * Player hits — draws one card. If busted, automatically advances turn.
     * Returns true if the player busted.
     */
    @Documented(description = "Player hits: draws one card, auto-advances turn if busted")
    public boolean playerHit(Player player) {
        validatePlayerTurn(player);

        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);

        if (player.getHand().isBusted()) {
            // Auto-stand on bust — move to next phase
            turnManager.nextTurn();
            return true;
        }

        return false;
    }

    /**
     * Player stands — ends their turn, advances to next phase.
     */
    public void playerStand(Player player) {
        validatePlayerTurn(player);
        turnManager.nextTurn();
    }

    /**
     * Player doubles down — doubles the bet, draws exactly one card, then stands.
     * Available only if player has exactly 2 cards and enough chips.
     */
    public boolean playerDoubleDown(Player player) {
        validatePlayerTurn(player);

        if (player.getHand().size() != 2) {
            throw new IllegalStateException("Can only double down with exactly 2 cards");
        }

        int additionalBet = player.getCurrentBet();
        if (player.getChips() < additionalBet) {
            throw new IllegalStateException("Insufficient chips to double down");
        }

        player.removeChips(additionalBet);
        player.setCurrentBet(player.getCurrentBet() + additionalBet);

        if (gameState.isMultiplayer()) {
            gameState.addToPot(additionalBet);
        }

        // Draw exactly one card
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);

        // Automatically stand after double down
        turnManager.nextTurn();

        return player.getHand().isBusted();
    }

    /**
     * Player splits — splits a pair into two separate hands.
     * Each hand gets one additional card.
     */
    public void playerSplit(Player player) {
        validatePlayerTurn(player);

        Hand originalHand = player.getHand();
        if (!originalHand.canSplit()) {
            throw new IllegalStateException("Cannot split — hand must have exactly 2 cards of the same rank");
        }

        int splitBet = player.getCurrentBet();
        if (player.getChips() < splitBet) {
            throw new IllegalStateException("Insufficient chips to split");
        }

        player.removeChips(splitBet);

        if (gameState.isMultiplayer()) {
            gameState.addToPot(splitBet);
        }

        // Create split hand with the second card
        Hand splitHand = new Hand();
        Card secondCard = originalHand.getCardsMutable().remove(1);
        splitHand.addCard(secondCard);

        // Deal one new card to each hand
        Card newCard1 = gameState.getDeck().drawCard();
        newCard1.setFaceUp(true);
        originalHand.addCard(newCard1);

        Card newCard2 = gameState.getDeck().drawCard();
        newCard2.setFaceUp(true);
        splitHand.addCard(newCard2);

        player.setSplitHand(splitHand);
    }

    /**
     * Draws one card into the player's main hand during a split (hand 1).
     * Does NOT advance the turn — the controller decides whether to switch to hand 2.
     * Returns true if the main hand is now busted.
     */
    public boolean playerHitDuringSplit(Player player) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);
        return player.getHand().isBusted();
    }

    /**
     * Draws one card into the player's split hand (hand 2).
     * Does NOT advance the turn — the controller handles bust/stand transitions.
     * Returns true if the split hand is now busted.
     */
    public boolean playerHitSplitHand(Player player) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getSplitHand().addCard(card);
        return player.getSplitHand().isBusted();
    }

    // ========================================================================
    // DEALER TURN (SP only)
    // ========================================================================

    /**
     * Executes the dealer's turn. Dealer reveals hole card, then hits according to rules.
     * Only used in SINGLE_PLAYER mode.
     */
    public void executeDealerTurn() {
        if (!gameState.isSinglePlayer()) {
            throw new IllegalStateException("Dealer turn is only for single-player mode");
        }

        Dealer dealer = gameState.getDealer();

        // Reveal hole card
        for (Card card : dealer.getHand().getCards()) {
            card.setFaceUp(true);
        }

        // Dealer draws according to rules
        while (dealer.shouldHit()) {
            Card card = gameState.getDeck().drawCard();
            card.setFaceUp(true);
            dealer.getHand().addCard(card);
        }

        // Advance to showdown
        turnManager.nextTurn();
    }

    // ========================================================================
    // SHOWDOWN & RESOLUTION
    // ========================================================================

    /**
     * Resolves the round — determines winner and distributes chips/pot.
     * Returns the GameResult.
     */
    @Documented(description = "Resolves the round: determines winner, distributes chips/pot based on game mode")
    public GameResult resolveShowdown() {
        GameResult result;

        if (gameState.isSinglePlayer()) {
            result = resolveSinglePlayer();
        } else {
            result = resolveMultiplayer();
        }

        gameState.setPhase(GamePhase.ROUND_OVER);
        return result;
    }

    private GameResult resolveSinglePlayer() {
        Player player = gameState.getPlayer1();
        Dealer dealer = gameState.getDealer();

        if (player.hasSplit()) {
            return resolveSplitHands(player, dealer);
        }

        GameResult result = BlackjackRules.determineWinnerSP(player, dealer);
        double multiplier = BlackjackRules.getPayoutMultiplier(result);

        int payout = (int) (player.getCurrentBet() * multiplier);
        player.addChips(payout);

        gameState.setResultMessage(formatResultMessageSP(result, player, dealer));
        return result;
    }

    /**
     * Resolves both split hands independently against the dealer.
     * Each hand has the same bet amount and is evaluated separately.
     */
    private GameResult resolveSplitHands(Player player, Dealer dealer) {
        Hand hand1 = player.getHand();
        Hand hand2 = player.getSplitHand();
        Hand dealerHand = dealer.getHand();
        int betPerHand = player.getCurrentBet(); // This is the original bet (split costs same again)

        GameResult result1 = BlackjackRules.determineWinnerForHand(hand1, dealerHand);
        GameResult result2 = BlackjackRules.determineWinnerForHand(hand2, dealerHand);

        // Payout for each hand
        int payout1 = calculateSplitPayout(result1, betPerHand);
        int payout2 = calculateSplitPayout(result2, betPerHand);
        player.addChips(payout1 + payout2);

        // Build result message
        int dealerScore = dealerHand.calculateScore();
        String msg = "Split Results vs Dealer (" + dealerScore + "):\n"
                + "  Hand 1 (" + hand1.calculateScore() + "): " + describeResult(result1, payout1) + "\n"
                + "  Hand 2 (" + hand2.calculateScore() + "): " + describeResult(result2, payout2) + "\n"
                + "  Net: " + (payout1 + payout2 - 2 * betPerHand) + " chips";
        gameState.setResultMessage(msg);

        // Return the "better" result for popup purposes
        if (result1 == GameResult.PLAYER_WINS || result2 == GameResult.PLAYER_WINS) {
            return GameResult.PLAYER_WINS;
        }
        if (result1 == GameResult.PUSH || result2 == GameResult.PUSH) {
            return GameResult.PUSH;
        }
        return GameResult.DEALER_WINS;
    }

    private int calculateSplitPayout(GameResult result, int bet) {
        return switch (result) {
            case PLAYER_WINS -> bet * 2;    // win: get bet back + winnings
            case PUSH -> bet;               // push: bet returned
            default -> 0;                   // loss: lose the bet
        };
    }

    private String describeResult(GameResult result, int payout) {
        return switch (result) {
            case PLAYER_WINS -> "WIN (+" + payout + ")";
            case PUSH -> "PUSH (returned " + payout + ")";
            case DEALER_WINS -> "LOSS";
            default -> "???";
        };
    }

    private GameResult resolveMultiplayer() {
        Player p1 = gameState.getPlayer1();
        Player p2 = gameState.getPlayer2();

        // Reveal all cards at showdown
        revealAllCards(p1);
        revealAllCards(p2);

        GameResult result = BlackjackRules.determineWinnerMP(p1, p2);
        int pot = gameState.getPot();

        switch (result) {
            case PLAYER1_WINS -> p1.addChips(pot);
            case PLAYER2_WINS -> p2.addChips(pot);
            case PUSH -> {
                // Return stakes equally
                p1.addChips(pot / 2);
                p2.addChips(pot - pot / 2); // handles odd pot
            }
            default -> {} // shouldn't happen in MP
        }

        gameState.setResultMessage(formatResultMessageMP(result, p1, p2));
        return result;
    }

    private void revealAllCards(Player player) {
        for (Card card : player.getHand().getCards()) {
            card.setFaceUp(true);
        }
        if (player.hasSplit()) {
            for (Card card : player.getSplitHand().getCards()) {
                card.setFaceUp(true);
            }
        }
    }

    private String formatResultMessageSP(GameResult result, Player player, Dealer dealer) {
        int playerScore = player.getHand().calculateScore();
        int dealerScore = dealer.getHand().calculateScore();

        return switch (result) {
            case BLACKJACK -> "Blackjack! You win " + (int)(player.getCurrentBet() * 1.5) + " chips!";
            case PLAYER_WINS -> "You win! (" + playerScore + " vs " + dealerScore + ")";
            case DEALER_WINS -> "Dealer wins. (" + playerScore + " vs " + dealerScore + ")";
            case PUSH -> "Push! (" + playerScore + " vs " + dealerScore + ") Bet returned.";
            default -> "Round over.";
        };
    }

    private String formatResultMessageMP(GameResult result, Player p1, Player p2) {
        int score1 = p1.getHand().calculateScore();
        int score2 = p2.getHand().calculateScore();

        return switch (result) {
            case PLAYER1_WINS -> p1.getName() + " wins! (" + score1 + " vs " + score2 + ") — wins the pot of " + gameState.getPot();
            case PLAYER2_WINS -> p2.getName() + " wins! (" + score2 + " vs " + score1 + ") — wins the pot of " + gameState.getPot();
            case PUSH -> "Push! (" + score1 + " vs " + score2 + ") — pot returned.";
            default -> "Round over.";
        };
    }

    // ========================================================================
    // NETWORK SUPPORT — Card visibility filtering for MP
    // ========================================================================

    /**
     * Returns a deep copy of the GameState where the opponent's cards are hidden.
     * Used by the server to send filtered state to each client in MP.
     * The opponent's card count is preserved but rank/suit are hidden (face-down).
     *
     * @param playerId the player requesting the state (1 or 2)
     * @return filtered GameState safe to send to that player
     */
    @Documented(description = "Returns filtered GameState for a specific player, hiding opponent cards in multiplayer")
    public GameState getVisibleStateForPlayer(int playerId) {
        // During showdown or round over, return full state (all cards visible)
        if (gameState.getPhase() == GamePhase.SHOWDOWN
                || gameState.getPhase() == GamePhase.ROUND_OVER) {
            return gameState;
        }

        // Create a copy with opponent's cards hidden
        GameState filtered = new GameState(gameState.getMode());
        filtered.setPhase(gameState.getPhase());
        filtered.setDeck(gameState.getDeck());
        filtered.setPot(gameState.getPot());
        filtered.setRoundNumber(gameState.getRoundNumber());
        filtered.setResultMessage(gameState.getResultMessage());
        filtered.setTimestamp(gameState.getTimestamp());

        if (playerId == 1) {
            filtered.setPlayer1(gameState.getPlayer1());
            filtered.setPlayer2(createHiddenPlayer(gameState.getPlayer2()));
        } else {
            filtered.setPlayer1(createHiddenPlayer(gameState.getPlayer1()));
            filtered.setPlayer2(gameState.getPlayer2());
        }

        return filtered;
    }

    /**
     * Creates a copy of a player with only the first card visible.
     * The first card is shown face-up, remaining cards are face-down placeholders.
     */
    private Player createHiddenPlayer(Player original) {
        if (original == null) return null;

        Player hidden = new Player(original.getName(), original.getChips());
        hidden.setCurrentBet(original.getCurrentBet());
        hidden.setPlayerId(original.getPlayerId());

        for (int i = 0; i < original.getHand().size(); i++) {
            if (i == 0) {
                // First card — show the real card face-up
                Card realCard = original.getHand().getCards().get(0);
                Card visibleCopy = new Card(realCard.getRank(), realCard.getSuit());
                visibleCopy.setFaceUp(true);
                hidden.getHand().addCard(visibleCopy);
            } else {
                // Remaining cards — face-down placeholders
                Card placeholder = new Card(Rank.ACE, Suit.SPADES);
                placeholder.setFaceUp(false);
                hidden.getHand().addCard(placeholder);
            }
        }

        return hidden;
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private void validatePlayerTurn(Player player) {
        if (!turnManager.isPlayerTurn(player)) {
            throw new IllegalStateException("It's not " + player.getName() + "'s turn");
        }
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    public GameState getGameState() { return gameState; }
    public TurnManager getTurnManager() { return turnManager; }
    public GameConfig getConfig() { return config; }
}


