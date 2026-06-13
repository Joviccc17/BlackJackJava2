package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

/**
 * Available player actions during a game round.
 */
public enum PlayerAction implements Serializable {
    HIT,
    STAND,
    DOUBLE_DOWN,
    SPLIT,
    SURRENDER
}

