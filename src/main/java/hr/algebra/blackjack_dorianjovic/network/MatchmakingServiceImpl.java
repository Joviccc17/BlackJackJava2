package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatchmakingServiceImpl extends UnicastRemoteObject implements MatchmakingService {

    private final List<String> waitingPlayers = new CopyOnWriteArrayList<>();
    private final int gameServerPort;
    private boolean gameStarted = false;

    public MatchmakingServiceImpl(int gameServerPort) throws RemoteException {
        super();
        this.gameServerPort = gameServerPort;
    }

    @Override
    public synchronized int registerPlayer(String name) throws RemoteException {
        int playerId = waitingPlayers.size() + 1;
        waitingPlayers.add(name);
        return playerId;
    }

    @Override
    public List<String> getWaitingPlayers() throws RemoteException {
        return new ArrayList<>(waitingPlayers);
    }

    @Override
    public int getGameServerPort() throws RemoteException {
        return gameServerPort;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
}
