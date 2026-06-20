package hr.algebra.blackjack_dorianjovic.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Card> cards = new ArrayList<>();

    private transient ObservableList<Card> observableCards;

    public Hand() {}

    public ObservableList<Card> observableCards() {
        if (observableCards == null) {
            observableCards = FXCollections.observableArrayList(cards);
        }
        return observableCards;
    }

    public void addCard(Card card) {
        cards.add(card);
        if (observableCards != null) observableCards.add(card);
    }

    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public List<Card> getCardsMutable() {
        return cards;
    }

    public int calculateScore() {
        int score = 0;
        int aceCount = 0;

        for (Card card : cards) {
            score += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && calculateScore() == 21;
    }

    public boolean canSplit() {
        return cards.size() == 2
                && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public int size() {
        return cards.size();
    }

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
