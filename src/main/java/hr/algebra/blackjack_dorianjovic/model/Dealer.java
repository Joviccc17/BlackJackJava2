package hr.algebra.blackjack_dorianjovic.model;

/**
 * Represents the dealer in single-player mode.
 * Extends Player with dealer-specific logic (hit/stand rules).
 * Used only in SINGLE_PLAYER mode — in MULTIPLAYER there is no dealer.
 */
public class Dealer extends Player {

    private static final long serialVersionUID = 1L;

    private final boolean hitsSoft17;

    /**
     * Creates a dealer with the specified soft-17 rule.
     * @param hitsSoft17 if true, dealer hits on soft 17; otherwise stands.
     */
    public Dealer(boolean hitsSoft17) {
        super("Dealer", Integer.MAX_VALUE); // Dealer has unlimited chips
        this.hitsSoft17 = hitsSoft17;
    }

    /**
     * Determines whether the dealer should take another card.
     * Standard rule: dealer hits on 16 or less, stands on 17+.
     * Optional: dealer hits on soft 17 if configured.
     */
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

    /**
     * Returns true if the dealer's hand is "soft" (contains an Ace counted as 11).
     */
    private boolean hasSoftHand() {
        int score = 0;
        int aceCount = 0;

        for (Card card : getHand().getCards()) {
            score += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }

        // If we have aces and the score without reducing any ace is <= 21,
        // then at least one ace is still counted as 11 (soft hand)
        return aceCount > 0 && score <= 21;
    }

    public boolean isHitsSoft17() {
        return hitsSoft17;
    }

    @Override
    public String toString() {
        return "Dealer " + getHand();
    }
}

