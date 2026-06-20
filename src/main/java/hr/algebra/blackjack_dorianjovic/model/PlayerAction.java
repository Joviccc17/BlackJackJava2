package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

public enum PlayerAction implements Serializable {
    HIT,
    STAND,
    DOUBLE_DOWN,
    SPLIT,
    HIT_SPLIT_HAND,
    STAND_SPLIT_HAND
}
