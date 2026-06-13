package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

/**
 * Game mode enum — determines which rule set is active.
 */
public enum GameMode implements Serializable {

    /**
     * Classic blackjack: player vs AI dealer.
     * Dealer hits on 16 or less, stands on 17+.
     */
    SINGLE_PLAYER,

    /**
     * PvP mode: two players compete against each other, no dealer.
     * Both place stakes, draw cards, closest to 21 wins.
     * Players cannot see each other's cards until showdown.
     */
    MULTIPLAYER
}

