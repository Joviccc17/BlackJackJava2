package hr.algebra.blackjack_dorianjovic.threading;

import hr.algebra.blackjack_dorianjovic.engine.GameEngine;
import hr.algebra.blackjack_dorianjovic.model.Card;
import hr.algebra.blackjack_dorianjovic.model.Dealer;
import javafx.concurrent.Task;

import java.util.function.Consumer;

/**
 * JavaFX Task that executes the dealer's turn asynchronously with delays
 * between each card draw, so the player can watch the dealer play.
 * Only used in SINGLE_PLAYER mode.
 */
public class DealerPlayTask extends Task<Void> {

    private final GameEngine engine;
    private final Consumer<String> onDealerDraw;
    private final long delayMs;

    /**
     * @param engine       the game engine
     * @param onDealerDraw callback invoked after each dealer card draw
     * @param delayMs      delay in milliseconds between each draw
     */
    public DealerPlayTask(GameEngine engine, Consumer<String> onDealerDraw, long delayMs) {
        this.engine = engine;
        this.onDealerDraw = onDealerDraw;
        this.delayMs = delayMs;
    }

    @Override
    protected Void call() throws Exception {
        Dealer dealer = engine.getGameState().getDealer();

        // Pause before revealing
        Thread.sleep(delayMs / 2);

        // Reveal hole card
        for (Card card : dealer.getHand().getCards()) {
            card.setFaceUp(true);
        }
        updateMessage("Dealer reveals hole card...");

        if (onDealerDraw != null) {
            onDealerDraw.accept("Hole card revealed");
        }

        // Pause after revealing hole card
        Thread.sleep(delayMs);

        // Dealer draws according to rules — one card at a time with delay
        while (dealer.shouldHit()) {
            Card card = engine.getGameState().getDeck().drawCard();
            card.setFaceUp(true);
            dealer.getHand().addCard(card);

            int score = dealer.getHand().calculateScore();
            updateMessage("Dealer draws: " + card + " (Score: " + score + ")");

            if (onDealerDraw != null) {
                onDealerDraw.accept("Dealer drew " + card);
            }

            // Wait between draws so player can see each card
            Thread.sleep(delayMs);
        }

        int finalScore = dealer.getHand().calculateScore();
        if (dealer.getHand().isBusted()) {
            updateMessage("Dealer busts with " + finalScore + "!");
        } else {
            updateMessage("Dealer stands at " + finalScore);
        }

        return null;
    }
}

