package hr.algebra.blackjack_dorianjovic.view;

import hr.algebra.blackjack_dorianjovic.model.Card;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Custom JavaFX component that displays a hand of cards as a horizontal row.
 * Automatically creates CardView nodes for each card in the hand.
 */
public class HandView extends HBox {

    private static final double CARD_SPACING = 10;

    public HandView() {
        setSpacing(CARD_SPACING);
        setAlignment(Pos.CENTER);
    }

    /**
     * Updates the display with the given list of cards.
     * Clears existing cards and creates new CardView nodes.
     */
    public void updateCards(List<Card> cards) {
        getChildren().clear();
        for (Card card : cards) {
            getChildren().add(new CardView(card));
        }
    }

    /**
     * Binds this view to an ObservableList<Card>. Does an initial render,
     * then attaches a ListChangeListener so future changes auto-update the display.
     * Only call this from the JavaFX Application Thread.
     */
    public void bindToObservableList(ObservableList<Card> cards) {
        updateCards(cards);
        cards.addListener((ListChangeListener<Card>) change -> updateCards(cards));
    }

    /**
     * Adds a single card to the display with optional animation.
     */
    public void addCard(Card card) {
        CardView cardView = new CardView(card);
        getChildren().add(cardView);
    }

    /**
     * Clears all cards from the display.
     */
    public void clearCards() {
        getChildren().clear();
    }

    /**
     * Refreshes all card views (e.g., after cards are flipped face-up).
     */
    public void refreshAll() {
        for (var node : getChildren()) {
            if (node instanceof CardView cardView) {
                cardView.refresh();
            }
        }
    }
}

