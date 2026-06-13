package hr.algebra.blackjack_dorianjovic.view;

import hr.algebra.blackjack_dorianjovic.model.Card;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Custom JavaFX component that visually renders a playing card.
 * Shows rank + suit when face-up, or a card back pattern when face-down.
 */
public class CardView extends StackPane {

    private static final double CARD_WIDTH = 80;
    private static final double CARD_HEIGHT = 120;
    private static final double ARC_SIZE = 10;

    private final Card card;

    public CardView(Card card) {
        this.card = card;
        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMinSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        setAlignment(Pos.CENTER);
        render();
    }

    /**
     * Renders the card based on its face-up/face-down state.
     */
    private void render() {
        getChildren().clear();

        if (card.isFaceUp()) {
            renderFaceUp();
        } else {
            renderFaceDown();
        }
    }

    /**
     * Renders face-up card: white background with rank and suit.
     */
    private void renderFaceUp() {
        Rectangle bg = createCardBackground(Color.WHITE);
        getChildren().add(bg);

        // Determine color based on suit
        Color suitColor = switch (card.getSuit()) {
            case HEARTS, DIAMONDS -> Color.RED;
            case CLUBS, SPADES -> Color.BLACK;
        };

        // Rank text (top-left area)
        String displayText = card.getRank().getDisplayName() + "\n" + card.getSuit().getSymbol();
        Text rankText = new Text(displayText);
        rankText.setFont(Font.font("System", FontWeight.BOLD, 18));
        rankText.setFill(suitColor);

        // Large center suit symbol
        Text centerSuit = new Text(card.getSuit().getSymbol());
        centerSuit.setFont(Font.font("System", FontWeight.BOLD, 36));
        centerSuit.setFill(suitColor);
        centerSuit.setTranslateY(10);

        // Small rank in corner
        Text cornerRank = new Text(card.getRank().getDisplayName());
        cornerRank.setFont(Font.font("System", FontWeight.BOLD, 12));
        cornerRank.setFill(suitColor);
        cornerRank.setTranslateX(-CARD_WIDTH / 2 + 14);
        cornerRank.setTranslateY(-CARD_HEIGHT / 2 + 16);

        // Small suit in corner
        Text cornerSuit = new Text(card.getSuit().getSymbol());
        cornerSuit.setFont(Font.font("System", FontWeight.BOLD, 10));
        cornerSuit.setFill(suitColor);
        cornerSuit.setTranslateX(-CARD_WIDTH / 2 + 14);
        cornerSuit.setTranslateY(-CARD_HEIGHT / 2 + 30);

        getChildren().addAll(centerSuit, cornerRank, cornerSuit);
    }

    /**
     * Renders face-down card: blue patterned back.
     */
    private void renderFaceDown() {
        Rectangle bg = createCardBackground(Color.web("#1a3a6b"));
        getChildren().add(bg);

        // Inner border pattern
        Rectangle inner = new Rectangle(CARD_WIDTH - 16, CARD_HEIGHT - 16);
        inner.setArcWidth(ARC_SIZE - 2);
        inner.setArcHeight(ARC_SIZE - 2);
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web("#4a7abf"));
        inner.setStrokeWidth(2);

        // Center diamond pattern
        Text pattern = new Text("♦");
        pattern.setFont(Font.font("System", FontWeight.BOLD, 30));
        pattern.setFill(Color.web("#4a7abf"));

        getChildren().addAll(inner, pattern);
    }

    /**
     * Creates the card background rectangle with rounded corners and a border.
     */
    private Rectangle createCardBackground(Color fill) {
        Rectangle bg = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
        bg.setArcWidth(ARC_SIZE);
        bg.setArcHeight(ARC_SIZE);
        bg.setFill(fill);
        bg.setStroke(Color.DARKGRAY);
        bg.setStrokeWidth(1.5);
        return bg;
    }

    /**
     * Refreshes the card display (e.g., after flipping face-up).
     */
    public void refresh() {
        render();
    }

    public Card getCard() {
        return card;
    }
}

