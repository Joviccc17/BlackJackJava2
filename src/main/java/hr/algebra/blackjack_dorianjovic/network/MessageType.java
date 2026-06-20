package hr.algebra.blackjack_dorianjovic.network;

import java.io.Serializable;

public enum MessageType implements Serializable {

    BET,

    PLAYER_ACTION,

    STATE_UPDATE,

    PLAYER_JOINED,

    PLAYER_LEFT,

    GAME_START,

    CHAT,

    DEAL_REQUEST,

    SHOWDOWN,

    HEARTBEAT
}
