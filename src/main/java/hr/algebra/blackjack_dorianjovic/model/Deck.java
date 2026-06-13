package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck (shoe) of playing cards.
 * Supports multiple decks shuffled together.
 */
public class Deck implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Card> cards;
    private int currentIndex;

    /**
     * Creates a shoe with the specified number of standard 52-card decks.
     */
    public Deck(int numberOfDecks) {
        cards = new ArrayList<>(52 * numberOfDecks);
        for (int d = 0; d < numberOfDecks; d++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
        shuffle();
    }

    /**
     * Shuffles all cards and resets the draw position.
     */
    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
    }

    /**
     * Draws the next card from the deck.
     * If the deck is exhausted, it reshuffles automatically.
     *
     * @return the drawn Card
     */
    public Card drawCard() {
        if (currentIndex >= cards.size()) {
            shuffle();
        }
        return cards.get(currentIndex++);
    }

    /**
     * Returns the number of cards remaining in the shoe.
     */
    public int cardsRemaining() {
        return cards.size() - currentIndex;
    }

    /**
     * Returns the total number of cards in the shoe.
     */
    public int totalCards() {
        return cards.size();
    }

    @Override
    public String toString() {
        return "Deck{remaining=" + cardsRemaining() + "/" + totalCards() + "}";
    }
}

