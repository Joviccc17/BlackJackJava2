package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Remote interface for matchmaking — player registration and lobby management.
 */
public interface MatchmakingService extends Remote {

    /**
     * Registers a player and returns their assigned player ID.
     */
    int registerPlayer(String name) throws RemoteException;

    /**
     * Returns the list of currently waiting player names.
     */
    List<String> getWaitingPlayers() throws RemoteException;

    /**
     * Returns the number of connected players.
     */
    int getPlayerCount() throws RemoteException;

    /**
     * Returns the TCP game server port for direct connection.
     */
    int getGameServerPort() throws RemoteException;

    /**
     * Returns true if the game has started.
     */
    boolean isGameStarted() throws RemoteException;
}

