package hr.algebra.blackjack_dorianjovic.network;

import java.io.Serializable;

/**
 * Types of messages sent between server and clients.
 */
public enum MessageType implements Serializable {
    /** Player places a bet */
    BET,
    /** Player action: HIT, STAND, SPLIT, DOUBLE_DOWN */
    PLAYER_ACTION,
    /** Server sends updated game state to clients */
    STATE_UPDATE,
    /** A player joined the game */
    PLAYER_JOINED,
    /** A player left the game */
    PLAYER_LEFT,
    /** Game is starting */
    GAME_START,
    /** Chat message between players */
    CHAT,
    /** Request to start dealing */
    DEAL_REQUEST,
    /** Showdown — reveal all cards */
    SHOWDOWN,
    /** Server ping / client pong for heartbeat */
    HEARTBEAT
}

