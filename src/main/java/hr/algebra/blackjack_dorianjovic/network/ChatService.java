package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {

    void sendMessage(String sender, String text) throws RemoteException;

    List<String> getMessages(int sinceIndex) throws RemoteException;
}
