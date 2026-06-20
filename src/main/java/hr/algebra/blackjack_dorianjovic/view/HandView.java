package hr.algebra.blackjack_dorianjovic.view;

import hr.algebra.blackjack_dorianjovic.model.Card;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.util.List;

public class HandView extends HBox {

    private static final double CARD_SPACING = 10;

    public HandView() {
        setSpacing(CARD_SPACING);
        setAlignment(Pos.CENTER);
    }

    public void updateCards(List<Card> cards) {
        getChildren().clear();
        for (Card card : cards) {
            getChildren().add(new CardView(card));
        }
    }

    public void bindToObservableList(ObservableList<Card> cards) {
        updateCards(cards);
        cards.addListener((ListChangeListener<Card>) change -> updateCards(cards));
    }

}
