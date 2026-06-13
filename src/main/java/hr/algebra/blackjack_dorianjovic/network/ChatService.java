package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Remote interface for chat communication during the game.
 */
public interface ChatService extends Remote {

    /**
     * Sends a chat message.
     */
    void sendMessage(String sender, String text) throws RemoteException;

    /**
     * Gets all messages since the given index.
     * @param sinceIndex the index to start from (exclusive)
     * @return list of messages formatted as "sender: text"
     */
    List<String> getMessages(int sinceIndex) throws RemoteException;

    /**
     * Gets the total number of messages.
     */
    int getMessageCount() throws RemoteException;
}

