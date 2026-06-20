package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.config.GameConfig;
import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.util.Documented;

public class GameEngine {

    private GameState gameState;
    private TurnManager turnManager;
    private final GameConfig config;

    public GameEngine(GameConfig config, GameMode mode) {
        this.config = config;
        initializeGame(mode);
    }

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

    public GameEngine(GameConfig config, GameState savedState) {
        this.config = config;
        this.gameState = savedState;
        this.turnManager = new TurnManager(savedState);
    }

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

        if (gameState.getDeck().cardsRemaining() < gameState.getDeck().totalCards() / 4) {
            gameState.getDeck().shuffle();
        }

        gameState.setPhase(GamePhase.BETTING);
    }

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

    @Documented(description = "Deals initial cards: SP gives player 2 face-up, dealer 1 up + 1 down; MP gives both players 2 cards")
    public void dealInitialCards() {
        gameState.setPhase(GamePhase.DEALING);

        if (gameState.isSinglePlayer()) {

            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer1(), true);

            dealCardToPlayer(gameState.getDealer(), true);
            dealCardToPlayer(gameState.getDealer(), false);
        } else {

            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer2(), true);
            dealCardToPlayer(gameState.getPlayer1(), true);
            dealCardToPlayer(gameState.getPlayer2(), true);
        }

        turnManager.startTurns();

        if (gameState.isSinglePlayer()) {
            Player player = gameState.getPlayer1();
            Dealer dealer = gameState.getDealer();

            if (player.getHand().isBlackjack() || dealer.getHand().isBlackjack()) {

                for (Card c : dealer.getHand().getCards()) {
                    c.setFaceUp(true);
                }

                gameState.setPhase(GamePhase.SHOWDOWN);
            }
        }
    }

    private void dealCardToPlayer(Player player, boolean faceUp) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(faceUp);
        player.getHand().addCard(card);
    }

    @Documented(description = "Player hits: draws one card, auto-advances turn if busted")
    public boolean playerHit(Player player) {
        validatePlayerTurn(player);

        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);

        if (player.getHand().isBusted()) {

            turnManager.nextTurn();
            return true;
        }

        return false;
    }

    public void playerStand(Player player) {
        validatePlayerTurn(player);
        turnManager.nextTurn();
    }

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

        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);

        turnManager.nextTurn();

        return player.getHand().isBusted();
    }

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

        Hand splitHand = new Hand();
        Card secondCard = originalHand.getCardsMutable().remove(1);
        splitHand.addCard(secondCard);

        Card newCard1 = gameState.getDeck().drawCard();
        newCard1.setFaceUp(true);
        originalHand.addCard(newCard1);

        Card newCard2 = gameState.getDeck().drawCard();
        newCard2.setFaceUp(true);
        splitHand.addCard(newCard2);

        player.setSplitHand(splitHand);
    }

    public boolean playerHitDuringSplit(Player player) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getHand().addCard(card);
        return player.getHand().isBusted();
    }

    public boolean playerHitSplitHand(Player player) {
        Card card = gameState.getDeck().drawCard();
        card.setFaceUp(true);
        player.getSplitHand().addCard(card);
        return player.getSplitHand().isBusted();
    }

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

    private GameResult resolveSplitHands(Player player, Dealer dealer) {
        Hand hand1 = player.getHand();
        Hand hand2 = player.getSplitHand();
        Hand dealerHand = dealer.getHand();
        int betPerHand = player.getCurrentBet();

        GameResult result1 = BlackjackRules.determineWinnerForHand(hand1, dealerHand);
        GameResult result2 = BlackjackRules.determineWinnerForHand(hand2, dealerHand);

        int payout1 = calculateSplitPayout(result1, betPerHand);
        int payout2 = calculateSplitPayout(result2, betPerHand);
        player.addChips(payout1 + payout2);

        int dealerScore = dealerHand.calculateScore();
        String msg = "Split Results vs Dealer (" + dealerScore + "):\n"
                + "  Hand 1 (" + hand1.calculateScore() + "): " + describeResult(result1, payout1) + "\n"
                + "  Hand 2 (" + hand2.calculateScore() + "): " + describeResult(result2, payout2) + "\n"
                + "  Net: " + (payout1 + payout2 - 2 * betPerHand) + " chips";
        gameState.setResultMessage(msg);

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
            case PLAYER_WINS -> bet * 2;
            case PUSH -> bet;
            default -> 0;
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

        revealAllCards(p1);
        revealAllCards(p2);

        int score1 = bestScore(p1);
        int score2 = bestScore(p2);

        GameResult result = determineWinnerByScore(score1, score2);
        int pot = gameState.getPot();

        switch (result) {
            case PLAYER1_WINS -> p1.addChips(pot);
            case PLAYER2_WINS -> p2.addChips(pot);
            case PUSH -> {
                p1.addChips(pot / 2);
                p2.addChips(pot - pot / 2);
            }
            default -> {}
        }

        gameState.setResultMessage(formatResultMessageMP(result, p1, p2, score1, score2));
        return result;
    }

    private int bestScore(Player player) {
        int mainScore = player.getHand().isBusted() ? 0 : player.getHand().calculateScore();
        if (!player.hasSplit()) return mainScore;
        int splitScore = player.getSplitHand().isBusted() ? 0 : player.getSplitHand().calculateScore();
        return Math.max(mainScore, splitScore);
    }

    private GameResult determineWinnerByScore(int score1, int score2) {
        if (score1 == 0 && score2 == 0) return GameResult.PUSH;
        if (score1 > score2) return GameResult.PLAYER1_WINS;
        if (score2 > score1) return GameResult.PLAYER2_WINS;
        return GameResult.PUSH;
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

    private String formatResultMessageMP(GameResult result, Player p1, Player p2,
                                            int bestScore1, int bestScore2) {
        String s1 = formatPlayerScore(p1);
        String s2 = formatPlayerScore(p2);

        return switch (result) {
            case PLAYER1_WINS -> p1.getName() + " wins! (" + s1 + " vs " + s2 + ") — wins the pot of " + gameState.getPot();
            case PLAYER2_WINS -> p2.getName() + " wins! (" + s2 + " vs " + s1 + ") — wins the pot of " + gameState.getPot();
            case PUSH -> "Push! (" + s1 + " vs " + s2 + ") — pot returned.";
            default -> "Round over.";
        };
    }

    private String formatPlayerScore(Player player) {
        if (!player.hasSplit()) {
            return String.valueOf(player.getHand().calculateScore());
        }
        int h1 = player.getHand().calculateScore();
        int h2 = player.getSplitHand().calculateScore();
        return "H1:" + h1 + "/H2:" + h2;
    }

    @Documented(description = "Returns filtered GameState for a specific player, hiding opponent cards in multiplayer")
    public GameState getVisibleStateForPlayer(int playerId) {

        if (gameState.getPhase() == GamePhase.SHOWDOWN
                || gameState.getPhase() == GamePhase.ROUND_OVER) {
            return gameState;
        }

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

    private Player createHiddenPlayer(Player original) {
        if (original == null) return null;

        Player hidden = new Player(original.getName(), original.getChips());
        hidden.setCurrentBet(original.getCurrentBet());
        hidden.setPlayerId(original.getPlayerId());

        hideHandInto(original.getHand(), hidden.getHand());

        if (original.hasSplit()) {
            Hand hiddenSplit = new Hand();
            hideHandInto(original.getSplitHand(), hiddenSplit);
            hidden.setSplitHand(hiddenSplit);
        }

        return hidden;
    }

    private void hideHandInto(Hand source, Hand target) {
        for (int i = 0; i < source.size(); i++) {
            if (i == 0) {
                Card realCard = source.getCards().get(0);
                Card visibleCopy = new Card(realCard.getRank(), realCard.getSuit());
                visibleCopy.setFaceUp(true);
                target.addCard(visibleCopy);
            } else {
                Card placeholder = new Card(Rank.ACE, Suit.SPADES);
                placeholder.setFaceUp(false);
                target.addCard(placeholder);
            }
        }
    }

    private void validatePlayerTurn(Player player) {
        if (!turnManager.isPlayerTurn(player)) {
            throw new IllegalStateException("It's not " + player.getName() + "'s turn");
        }
    }

    public GameState getGameState() { return gameState; }
    public TurnManager getTurnManager() { return turnManager; }
    public GameConfig getConfig() { return config; }
}
