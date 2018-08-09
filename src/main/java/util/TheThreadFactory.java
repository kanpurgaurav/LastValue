package util;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class TheThreadFactory implements ThreadFactory {

    private static final Logger LOG = Logger.getLogger(TheThreadFactory.class.getName());

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOG.severe(String.format("Exception in thread %s with error %s",thread, e.getMessage()));
            }
        });
        return thread;
    }
}
