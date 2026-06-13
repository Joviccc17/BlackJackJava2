package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

/**
 * Represents the current phase of a game round.
 * Different phases are active depending on GameMode.
 */
public enum GamePhase implements Serializable {

    /** Waiting for players to connect (multiplayer). */
    WAITING,

    /** Players are placing their bets/stakes. */
    BETTING,

    /** Initial cards are being dealt. */
    DEALING,

    /** Player 1's turn to act (both SP and MP). */
    PLAYER_TURN,

    /** Player 2's turn to act (MP only — skipped in SP). */
    PLAYER2_TURN,

    /** Dealer's turn to draw cards (SP only — skipped in MP). */
    DEALER_TURN,

    /** All cards revealed, winner determined. */
    SHOWDOWN,

    /** Round is over, results displayed. */
    ROUND_OVER
}

