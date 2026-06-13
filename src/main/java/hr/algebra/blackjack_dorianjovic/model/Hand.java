package hr.algebra.blackjack_dorianjovic.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hand of cards held by a player or dealer.
 * Automatically calculates the optimal score (handling soft/hard Ace).
 * Cards are stored in a serializable ArrayList; an ObservableList mirror is
 * provided for JavaFX binding (transient — rebuilt on first access).
 */
public class Hand implements Serializable {

    private static final long serialVersionUID = 1L;

    // Serialized backing list
    private final List<Card> cards = new ArrayList<>();

    // Transient FX-observable mirror; kept in sync by addCard() and clear()
    private transient ObservableList<Card> observableCards;

    public Hand() {}

    /**
     * Returns the live ObservableList of cards for JavaFX binding.
     * Lazily initialised from the serialized backing list.
     */
    public ObservableList<Card> observableCards() {
        if (observableCards == null) {
            observableCards = FXCollections.observableArrayList(cards);
        }
        return observableCards;
    }

    /**
     * Adds a card to this hand, updating both the backing list and the
     * ObservableList (if it has been initialised).
     */
    public void addCard(Card card) {
        cards.add(card);
        if (observableCards != null) observableCards.add(card);
    }

    /**
     * Returns an unmodifiable view of the cards in this hand.
     */
    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    /**
     * Returns the mutable backing list (for engine use, e.g. playerSplit remove).
     */
    public List<Card> getCardsMutable() {
        return cards;
    }

    /**
     * Calculates the best score for this hand.
     * Aces count as 11 unless that would cause a bust, then they count as 1.
     */
    public int calculateScore() {
        int score = 0;
        int aceCount = 0;

        for (Card card : cards) {
            score += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        // Convert Aces from 11 to 1 as needed to avoid busting
        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    /**
     * Returns true if the hand score exceeds 21.
     */
    public boolean isBusted() {
        return calculateScore() > 21;
    }

    /**
     * Returns true if this is a natural blackjack (exactly 2 cards totaling 21).
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && calculateScore() == 21;
    }

    /**
     * Returns true if the hand can be split (exactly 2 cards of the same rank).
     */
    public boolean canSplit() {
        return cards.size() == 2
                && cards.get(0).getRank() == cards.get(1).getRank();
    }

    /**
     * Returns the number of cards in this hand.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Clears all cards from this hand, updating both lists.
     */
    public void clear() {
        cards.clear();
        if (observableCards != null) observableCards.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(cards.get(i));
        }
        sb.append("] = ").append(calculateScore());
        return sb.toString();
    }
}

