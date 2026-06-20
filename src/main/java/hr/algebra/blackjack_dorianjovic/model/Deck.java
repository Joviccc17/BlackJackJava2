package hr.algebra.blackjack_dorianjovic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Card> cards;
    private int currentIndex;

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

    public void shuffle() {
        Collections.shuffle(cards);
        currentIndex = 0;
    }

    public Card drawCard() {
        if (currentIndex >= cards.size()) {
            shuffle();
        }
        return cards.get(currentIndex++);
    }

    public int cardsRemaining() {
        return cards.size() - currentIndex;
    }

    public int totalCards() {
        return cards.size();
    }

    @Override
    public String toString() {
        return "Deck{remaining=" + cardsRemaining() + "/" + totalCards() + "}";
    }
}
