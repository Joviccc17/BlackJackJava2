package hr.algebra.blackjack_dorianjovic.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RMI implementation of ChatService.
 * Stores chat messages and allows clients to retrieve them.
 */
public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {

    private final List<String> messages = new CopyOnWriteArrayList<>();

    public ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void sendMessage(String sender, String text) throws RemoteException {
        String formatted = sender + ": " + text;
        messages.add(formatted);
    }

    @Override
    public List<String> getMessages(int sinceIndex) throws RemoteException {
        if (sinceIndex < 0) sinceIndex = 0;
        if (sinceIndex >= messages.size()) return new ArrayList<>();
        return new ArrayList<>(messages.subList(sinceIndex, messages.size()));
    }

    @Override
    public int getMessageCount() throws RemoteException {
        return messages.size();
    }
}

