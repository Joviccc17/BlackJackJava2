package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

/**
 * Represents card ranks from ACE to KING.
 * ACE has a primary value of 11 and an alternative value of 1.
 */
public enum Rank implements Serializable {

    ACE("A", 11),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 10),
    QUEEN("Q", 10),
    KING("K", 10);

    private final String displayName;
    private final int value;

    Rank(String displayName, int value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }

    /**
     * Returns the alternative value for Ace (1 instead of 11).
     * For all other ranks, returns the same value.
     */
    public int getAlternativeValue() {
        return this == ACE ? 1 : value;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

