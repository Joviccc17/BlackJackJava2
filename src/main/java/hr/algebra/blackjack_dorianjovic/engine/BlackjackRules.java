package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.util.Documented;

/**
 * Static utility class containing all Blackjack rule logic.
 * Has separate winner-determination methods for Single Player (vs dealer)
 * and Multiplayer (PvP, no dealer).
 */
public final class BlackjackRules {

    private BlackjackRules() {
        // Utility class — no instantiation
    }

    /**
     * Calculates the optimal score for a hand (handling soft Aces).
     */
    @Documented(description = "Calculates the optimal score for a hand, handling soft Aces (11 or 1)")
    public static int calculateScore(Hand hand) {
        return hand.calculateScore();
    }

    /**
     * Returns true if the hand's score exceeds 21.
     */
    public static boolean isBusted(Hand hand) {
        return hand.isBusted();
    }

    /**
     * Returns true if the hand is a natural blackjack (2 cards, score = 21).
     */
    public static boolean isBlackjack(Hand hand) {
        return hand.isBlackjack();
    }

    // ========================================================================
    // SINGLE PLAYER — Player vs Dealer
    // ========================================================================

    /**
     * Determines the result of a single-player round (player vs dealer).
     *
     * Rules:
     * - If player busts, dealer wins (regardless of dealer's hand)
     * - If dealer busts, player wins
     * - Natural blackjack beats a regular 21
     * - Higher score wins
     * - Equal scores = push
     */
    @Documented(description = "Determines single-player round result: player vs dealer with standard blackjack rules")
    public static GameResult determineWinnerSP(Player player, Dealer dealer) {
        Hand playerHand = player.getHand();
        Hand dealerHand = dealer.getHand();

        boolean playerBusted = playerHand.isBusted();
        boolean dealerBusted = dealerHand.isBusted();
        boolean playerBlackjack = playerHand.isBlackjack();
        boolean dealerBlackjack = dealerHand.isBlackjack();

        // Both have blackjack = push
        if (playerBlackjack && dealerBlackjack) {
            return GameResult.PUSH;
        }

        // Player has natural blackjack
        if (playerBlackjack) {
            return GameResult.BLACKJACK;
        }

        // Dealer has natural blackjack
        if (dealerBlackjack) {
            return GameResult.DEALER_WINS;
        }

        // Player busts — dealer wins
        if (playerBusted) {
            return GameResult.DEALER_WINS;
        }

        // Dealer busts — player wins
        if (dealerBusted) {
            return GameResult.PLAYER_WINS;
        }

        // Compare scores
        int playerScore = playerHand.calculateScore();
        int dealerScore = dealerHand.calculateScore();

        if (playerScore > dealerScore) {
            return GameResult.PLAYER_WINS;
        } else if (dealerScore > playerScore) {
            return GameResult.DEALER_WINS;
        } else {
            return GameResult.PUSH;
        }
    }

    /**
     * Determines the result for a specific hand against the dealer.
     * Used when evaluating split hands independently.
     */
    public static GameResult determineWinnerForHand(Hand playerHand, Hand dealerHand) {
        boolean playerBusted = playerHand.isBusted();
        boolean dealerBusted = dealerHand.isBusted();

        if (playerBusted) {
            return GameResult.DEALER_WINS;
        }
        if (dealerBusted) {
            return GameResult.PLAYER_WINS;
        }

        int playerScore = playerHand.calculateScore();
        int dealerScore = dealerHand.calculateScore();

        if (playerScore > dealerScore) {
            return GameResult.PLAYER_WINS;
        } else if (dealerScore > playerScore) {
            return GameResult.DEALER_WINS;
        } else {
            return GameResult.PUSH;
        }
    }

    // ========================================================================
    // MULTIPLAYER — Player vs Player (no dealer)
    // ========================================================================

    /**
     * Determines the result of a multiplayer round (player1 vs player2).
     *
     * Rules:
     * - If both bust, it's a push (pot returned equally)
     * - If one busts, the other wins
     * - Natural blackjack beats a regular 21
     * - Higher score wins
     * - Equal scores = push
     */
    @Documented(description = "Determines multiplayer PvP round result: closest to 21 wins, both bust = push")
    public static GameResult determineWinnerMP(Player player1, Player player2) {
        Hand hand1 = player1.getHand();
        Hand hand2 = player2.getHand();

        boolean p1Busted = hand1.isBusted();
        boolean p2Busted = hand2.isBusted();
        boolean p1Blackjack = hand1.isBlackjack();
        boolean p2Blackjack = hand2.isBlackjack();

        // Both bust = push
        if (p1Busted && p2Busted) {
            return GameResult.PUSH;
        }

        // Player 1 busts, player 2 doesn't
        if (p1Busted) {
            return GameResult.PLAYER2_WINS;
        }

        // Player 2 busts, player 1 doesn't
        if (p2Busted) {
            return GameResult.PLAYER1_WINS;
        }

        // Both have blackjack = push
        if (p1Blackjack && p2Blackjack) {
            return GameResult.PUSH;
        }

        // Only player 1 has blackjack
        if (p1Blackjack) {
            return GameResult.PLAYER1_WINS;
        }

        // Only player 2 has blackjack
        if (p2Blackjack) {
            return GameResult.PLAYER2_WINS;
        }

        // Compare scores — closest to 21 wins
        int score1 = hand1.calculateScore();
        int score2 = hand2.calculateScore();

        if (score1 > score2) {
            return GameResult.PLAYER1_WINS;
        } else if (score2 > score1) {
            return GameResult.PLAYER2_WINS;
        } else {
            return GameResult.PUSH;
        }
    }

    /**
     * Calculates the payout multiplier for a single-player result.
     * Blackjack pays 3:2 (1.5x), regular win pays 1:1.
     */
    public static double getPayoutMultiplier(GameResult result) {
        return switch (result) {
            case BLACKJACK -> 2.5;      // Original bet + 1.5x winnings
            case PLAYER_WINS -> 2.0;    // Original bet + 1x winnings
            case PUSH -> 1.0;           // Bet returned
            default -> 0.0;             // Loss — bet forfeited
        };
    }
}

