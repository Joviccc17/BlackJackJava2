package hr.algebra.blackjack_dorianjovic.network;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

public class RmiServiceManager {

    public static final String CHAT_SERVICE_NAME = "BlackjackChatService";
    public static final String MATCHMAKING_SERVICE_NAME = "BlackjackMatchmaking";
    public static final int RMI_REGISTRY_PORT = 1099;

    public static Registry startRegistry(int port) throws Exception {
        Registry registry = LocateRegistry.createRegistry(port);
        return registry;
    }

    public static void bindChatService(Registry registry, ChatServiceImpl chatService) throws Exception {
        registry.rebind(CHAT_SERVICE_NAME, chatService);
    }

    public static void bindMatchmakingService(Registry registry, MatchmakingServiceImpl matchmaking) throws Exception {
        registry.rebind(MATCHMAKING_SERVICE_NAME, matchmaking);
    }

    public static ChatService lookupChatService(String host, int registryPort) throws Exception {
        InitialContext ctx = createJndiContext(host, registryPort);
        return (ChatService) ctx.lookup(CHAT_SERVICE_NAME);
    }

    public static MatchmakingService lookupMatchmakingService(String host, int registryPort) throws Exception {
        InitialContext ctx = createJndiContext(host, registryPort);
        return (MatchmakingService) ctx.lookup(MATCHMAKING_SERVICE_NAME);
    }

    private static InitialContext createJndiContext(String host, int registryPort) throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://" + host + ":" + registryPort);
        return new InitialContext(env);
    }
}
