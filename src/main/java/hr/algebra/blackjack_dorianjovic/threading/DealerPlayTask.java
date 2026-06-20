package hr.algebra.blackjack_dorianjovic.threading;

import hr.algebra.blackjack_dorianjovic.engine.GameEngine;
import hr.algebra.blackjack_dorianjovic.model.Card;
import hr.algebra.blackjack_dorianjovic.model.Dealer;
import javafx.concurrent.Task;

import java.util.function.Consumer;

public class DealerPlayTask extends Task<Void> {

    private final GameEngine engine;
    private final Consumer<String> onDealerDraw;
    private final long delayMs;

    public DealerPlayTask(GameEngine engine, Consumer<String> onDealerDraw, long delayMs) {
        this.engine = engine;
        this.onDealerDraw = onDealerDraw;
        this.delayMs = delayMs;
    }

    @Override
    protected Void call() throws Exception {
        Dealer dealer = engine.getGameState().getDealer();

        Thread.sleep(delayMs / 2);

        for (Card card : dealer.getHand().getCards()) {
            card.setFaceUp(true);
        }
        updateMessage("Dealer reveals hole card...");

        if (onDealerDraw != null) {
            onDealerDraw.accept("Hole card revealed");
        }

        Thread.sleep(delayMs);

        while (dealer.shouldHit()) {
            Card card = engine.getGameState().getDeck().drawCard();
            card.setFaceUp(true);
            dealer.getHand().addCard(card);

            int score = dealer.getHand().calculateScore();
            updateMessage("Dealer draws: " + card + " (Score: " + score + ")");

            if (onDealerDraw != null) {
                onDealerDraw.accept("Dealer drew " + card);
            }

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
