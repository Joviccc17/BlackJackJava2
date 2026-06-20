package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

public enum GamePhase implements Serializable {

    WAITING,

    BETTING,

    DEALING,

    PLAYER_TURN,

    PLAYER2_TURN,

    DEALER_TURN,

    SHOWDOWN,

    ROUND_OVER
}
