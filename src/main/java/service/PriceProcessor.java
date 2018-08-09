package service;

import util.TheThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class PriceProcessor {

    private static final Logger LOG = Logger.getLogger(PriceProcessor.class.getName());

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2*Runtime.getRuntime().availableProcessors()
            , new TheThreadFactory());

    private static AtomicBoolean shouldRun = new AtomicBoolean(true);

    public static void startProcessing(BlockingQueue<FutureTask<String>> bq) {
        new TheThreadFactory().newThread(() -> {
            while (shouldRun.get()) {
                try {
                    executorService.submit(bq.take());
                } catch (InterruptedException e) {
                    LOG.severe("interrupted while polling from the queue, will do nothing as interruption not handled.");
                }
            }
        }).start();
    }

    public static void shutDown() {
        shouldRun.set(false);
        executorService.shutdown();
    }
}
