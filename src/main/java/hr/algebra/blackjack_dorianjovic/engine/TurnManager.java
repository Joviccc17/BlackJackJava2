package hr.algebra.blackjack_dorianjovic.engine;

import hr.algebra.blackjack_dorianjovic.model.*;

public class TurnManager {

    private final GameState gameState;
    private Player currentPlayer;

    public TurnManager(GameState gameState) {
        this.gameState = gameState;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void startTurns() {
        currentPlayer = gameState.getPlayer1();
        gameState.setPhase(GamePhase.PLAYER_TURN);
    }

    public GamePhase nextTurn() {
        GamePhase currentPhase = gameState.getPhase();

        if (gameState.isSinglePlayer()) {
            return advanceSinglePlayer(currentPhase);
        } else {
            return advanceMultiplayer(currentPhase);
        }
    }

    private GamePhase advanceSinglePlayer(GamePhase currentPhase) {
        GamePhase nextPhase = switch (currentPhase) {
            case PLAYER_TURN -> {

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

    public boolean isPlayerTurn(Player player) {
        return currentPlayer != null && currentPlayer == player;
    }
}
