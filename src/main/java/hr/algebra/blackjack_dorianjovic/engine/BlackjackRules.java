package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.model.*;
import hr.algebra.blackjack_dorianjovic.util.Documented;

public final class BlackjackRules {

    private BlackjackRules() {

    }

    @Documented(description = "Determines single-player round result: player vs dealer with standard blackjack rules")
    public static GameResult determineWinnerSP(Player player, Dealer dealer) {
        Hand playerHand = player.getHand();
        Hand dealerHand = dealer.getHand();

        boolean playerBusted = playerHand.isBusted();
        boolean dealerBusted = dealerHand.isBusted();
        boolean playerBlackjack = playerHand.isBlackjack();
        boolean dealerBlackjack = dealerHand.isBlackjack();

        if (playerBlackjack && dealerBlackjack) {
            return GameResult.PUSH;
        }

        if (playerBlackjack) {
            return GameResult.BLACKJACK;
        }

        if (dealerBlackjack) {
            return GameResult.DEALER_WINS;
        }

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

    @Documented(description = "Determines multiplayer PvP round result: closest to 21 wins, both bust = push")
    public static GameResult determineWinnerMP(Player player1, Player player2) {
        Hand hand1 = player1.getHand();
        Hand hand2 = player2.getHand();

        boolean p1Busted = hand1.isBusted();
        boolean p2Busted = hand2.isBusted();
        boolean p1Blackjack = hand1.isBlackjack();
        boolean p2Blackjack = hand2.isBlackjack();

        if (p1Busted && p2Busted) {
            return GameResult.PUSH;
        }

        if (p1Busted) {
            return GameResult.PLAYER2_WINS;
        }

        if (p2Busted) {
            return GameResult.PLAYER1_WINS;
        }

        if (p1Blackjack && p2Blackjack) {
            return GameResult.PUSH;
        }

        if (p1Blackjack) {
            return GameResult.PLAYER1_WINS;
        }

        if (p2Blackjack) {
            return GameResult.PLAYER2_WINS;
        }

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

    public static double getPayoutMultiplier(GameResult result) {
        return switch (result) {
            case BLACKJACK -> 2.5;
            case PLAYER_WINS -> 2.0;
            case PUSH -> 1.0;
            default -> 0.0;
        };
    }
}
