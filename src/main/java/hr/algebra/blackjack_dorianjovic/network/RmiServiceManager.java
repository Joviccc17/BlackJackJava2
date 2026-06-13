package hr.algebra.blackjack_dorianjovic.network;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

/**
 * JNDI/RMI utility for binding and looking up remote services.
 * Binding uses the RMI registry directly; lookup uses JNDI InitialContext.
 */
public class RmiServiceManager {

    public static final String CHAT_SERVICE_NAME = "BlackjackChatService";
    public static final String MATCHMAKING_SERVICE_NAME = "BlackjackMatchmaking";
    public static final int RMI_REGISTRY_PORT = 1099;

    /**
     * Starts an RMI registry on the specified port and binds services.
     * Called by the server/host.
     */
    public static Registry startRegistry(int port) throws Exception {
        Registry registry = LocateRegistry.createRegistry(port);
        return registry;
    }

    /**
     * Binds the ChatService to the RMI registry.
     */
    public static void bindChatService(Registry registry, ChatServiceImpl chatService) throws Exception {
        registry.rebind(CHAT_SERVICE_NAME, chatService);
    }

    /**
     * Binds the MatchmakingService to the RMI registry.
     */
    public static void bindMatchmakingService(Registry registry, MatchmakingServiceImpl matchmaking) throws Exception {
        registry.rebind(MATCHMAKING_SERVICE_NAME, matchmaking);
    }

    /**
     * Looks up the ChatService via JNDI InitialContext backed by the RMI registry.
     * Called by clients joining a game.
     */
    public static ChatService lookupChatService(String host, int registryPort) throws Exception {
        InitialContext ctx = createJndiContext(host, registryPort);
        return (ChatService) ctx.lookup(CHAT_SERVICE_NAME);
    }

    /**
     * Looks up the MatchmakingService via JNDI InitialContext backed by the RMI registry.
     */
    public static MatchmakingService lookupMatchmakingService(String host, int registryPort) throws Exception {
        InitialContext ctx = createJndiContext(host, registryPort);
        return (MatchmakingService) ctx.lookup(MATCHMAKING_SERVICE_NAME);
    }

    /**
     * Creates a JNDI InitialContext pointing at the RMI registry on the given host/port.
     */
    private static InitialContext createJndiContext(String host, int registryPort) throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://" + host + ":" + registryPort);
        return new InitialContext(env);
    }
}
