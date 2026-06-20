package hr.algebra.blackjack_dorianjovic.model;

public class Dealer extends Player {

    private static final long serialVersionUID = 1L;

    private final boolean hitsSoft17;

    public Dealer(boolean hitsSoft17) {
        super("Dealer", Integer.MAX_VALUE);
        this.hitsSoft17 = hitsSoft17;
    }

    public boolean shouldHit() {
        int score = getHand().calculateScore();

        if (score < 17) {
            return true;
        }

        if (score == 17 && hitsSoft17 && hasSoftHand()) {
            return true;
        }

        return false;
    }

    private boolean hasSoftHand() {
        int score = 0;
        int aceCount = 0;

        for (Card card : getHand().getCards()) {
            score += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        return aceCount > 0 && score <= 21;
    }

    @Override
    public String toString() {
        return "Dealer " + getHand();
    }
}
