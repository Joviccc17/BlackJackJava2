package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;

public class Card implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Rank rank;
    private final Suit suit;
    private boolean faceUp;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.faceUp = true;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public int getValue() {
        return rank.getValue();
    }

    @Override
    public String toString() {
        if (!faceUp) {
            return "??";
        }
        return rank.getDisplayName() + suit.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return 31 * rank.hashCode() + suit.hashCode();
    }
}
