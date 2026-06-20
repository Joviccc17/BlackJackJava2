package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MatchmakingService extends Remote {

    int registerPlayer(String name) throws RemoteException;

    List<String> getWaitingPlayers() throws RemoteException;

    int getGameServerPort() throws RemoteException;
}
