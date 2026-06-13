package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.model.*;

/**
 * Manages turn order for both game modes.
 *
 * Single Player flow:  PLAYER_TURN → DEALER_TURN → SHOWDOWN
 * Multiplayer flow:    PLAYER_TURN → PLAYER2_TURN → SHOWDOWN
 */
public class TurnManager {

    private final GameState gameState;
    private Player currentPlayer;

    public TurnManager(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Returns the player whose turn it currently is.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Sets the initial turn at the start of a round.
     * Always starts with player 1.
     */
    public void startTurns() {
        currentPlayer = gameState.getPlayer1();
        gameState.setPhase(GamePhase.PLAYER_TURN);
    }

    /**
     * Advances to the next turn/phase based on the current game mode.
     * Returns the new GamePhase after advancing.
     */
    public GamePhase nextTurn() {
        GamePhase currentPhase = gameState.getPhase();

        if (gameState.isSinglePlayer()) {
            return advanceSinglePlayer(currentPhase);
        } else {
            return advanceMultiplayer(currentPhase);
        }
    }

    /**
     * SP flow: PLAYER_TURN → DEALER_TURN → SHOWDOWN → ROUND_OVER
     */
    private GamePhase advanceSinglePlayer(GamePhase currentPhase) {
        GamePhase nextPhase = switch (currentPhase) {
            case PLAYER_TURN -> {
                // If player busted, skip dealer turn and go straight to showdown
                if (gameState.getPlayer1().getHand().isBusted()) {
                    yield GamePhase.SHOWDOWN;
                }
                currentPlayer = gameState.getDealer();
                yield GamePhase.DEALER_TURN;
            }
            case DEALER_TURN -> {
                currentPlayer = null;
                yield GamePhase.SHOWDOWN;
            }
            case SHOWDOWN -> GamePhase.ROUND_OVER;
            default -> currentPhase;
        };

        gameState.setPhase(nextPhase);
        return nextPhase;
    }

    /**
     * MP flow: PLAYER_TURN → PLAYER2_TURN → SHOWDOWN → ROUND_OVER
     * No dealer turn at all.
     */
    private GamePhase advanceMultiplayer(GamePhase currentPhase) {
        GamePhase nextPhase = switch (currentPhase) {
            case PLAYER_TURN -> {
                currentPlayer = gameState.getPlayer2();
                yield GamePhase.PLAYER2_TURN;
            }
            case PLAYER2_TURN -> {
                currentPlayer = null;
                yield GamePhase.SHOWDOWN;
            }
            case SHOWDOWN -> GamePhase.ROUND_OVER;
            default -> currentPhase;
        };

        gameState.setPhase(nextPhase);
        return nextPhase;
    }

    /**
     * Returns true if it's the given player's turn.
     */
    public boolean isPlayerTurn(Player player) {
        return currentPlayer != null && currentPlayer == player;
    }

    /**
     * Returns true if the round is over.
     */
    public boolean isRoundOver() {
        return gameState.getPhase() == GamePhase.ROUND_OVER;
    }
}

