package hr.algebra.blackjack_dorianjovic.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppExecutorService {

    private static AppExecutorService instance;

    private final ExecutorService cachedPool;
    private final ScheduledExecutorService scheduledPool;

    private AppExecutorService() {
        cachedPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("BlackJack-Worker-" + t.getId());
            return t;
        });

        scheduledPool = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("BlackJack-Scheduled-" + t.getId());
            return t;
        });
    }

    public static synchronized AppExecutorService getInstance() {
        if (instance == null) {
            instance = new AppExecutorService();
        }
        return instance;
    }

    public void submit(Runnable task) {
        cachedPool.submit(task);
    }

    public ScheduledExecutorService getScheduledPool() {
        return scheduledPool;
    }

    public void shutdown() {
        cachedPool.shutdown();
        scheduledPool.shutdown();
        try {
            if (!cachedPool.awaitTermination(3, TimeUnit.SECONDS)) {
                cachedPool.shutdownNow();
            }
            if (!scheduledPool.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduledPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            cachedPool.shutdownNow();
            scheduledPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
