package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

/**
 * Result of a round for a given player.
 */
public enum GameResult implements Serializable {

    /** Player wins (SP: beat dealer, MP: closer to 21). */
    PLAYER_WINS,

    /** Player 1 wins in multiplayer. */
    PLAYER1_WINS,

    /** Player 2 wins in multiplayer. */
    PLAYER2_WINS,

    /** Dealer wins (SP only). */
    DEALER_WINS,

    /** Tie — bets returned. */
    PUSH,

    /** Natural blackjack (2 cards = 21). */
    BLACKJACK
}

