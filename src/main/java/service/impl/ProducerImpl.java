package service.impl;

import cache.PriceCache;
import cache.TempCache;
import data.Chunk;
import data.Price;
import exception.UnsupportedOperationException;
import service.ProducerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


public class ProducerImpl implements ProducerService {

    private static final Logger LOG = Logger.getLogger(ProducerImpl.class.getName());
    private final BlockingQueue<FutureTask<String>> futureTaskBlockingQueue;
    private Map<String, List<FutureTask<String>>> batchIdFutureMap;

    public ProducerImpl(BlockingQueue<FutureTask<String>> futureTaskBlockingQueue, Map<String, List<FutureTask<String>>> batchIdFutureMap) {
        this.futureTaskBlockingQueue = futureTaskBlockingQueue;
        this.batchIdFutureMap = batchIdFutureMap;
    }

    @Override
    public boolean startProduction(final String batchId) throws UnsupportedOperationException {
        synchronized (batchId) {
            Map<String, Price> tmpMap = TempCache.get(batchId);
            if (null != tmpMap) {
                throw new UnsupportedOperationException(String.format("You cannot start this batch same batch with batchId %s is alerady started \n " +
                        "In order to startProcessing this batch again either cancel the previous batch or use a different batch id", batchId));
            }
            TempCache.startBatch(batchId);
        }
        return true;
    }

    @Override
    public boolean cancelBatch(final String batchId) throws UnsupportedOperationException {
        synchronized (batchId) {
            if (!TempCache.tmpCacheContains(batchId) || TempCache.get(batchId).size() < 1) {
                throw new UnsupportedOperationException("You cann not cancel this batch, it is either completed or already been cancelled");
            }
            TempCache.remove(batchId);
            batchIdFutureMap.get(batchId).forEach(futureTask -> futureTask.cancel(true));
        }
        return true;
    }

    @Override
    public boolean completeBatch(final String batchId) throws UnsupportedOperationException {
        synchronized (batchId) {
            if (!TempCache.tmpCacheContains(batchId)) {
                throw new UnsupportedOperationException("You cann not complete this batch, it is either cancelled or already been completed");
            }
            batchIdFutureMap.get(batchId).forEach(futureTask -> {
                try {
                    futureTask.get();
                } catch (InterruptedException e) {
                    LOG.severe("Interrupted while completing a chunk "+e.getMessage());
                } catch (ExecutionException e) {
                    LOG.severe("ExecutionException while completing a chunk "+e.getMessage());
                }
            });
            updatePriceCache(batchId);
            TempCache.remove(batchId);
        }
        return true;
    }

    private void updatePriceCache(String batchId) {
        Map<String, Price> tempCachemap = TempCache.get(batchId);
        tempCachemap.keySet().forEach(key -> {
            Price tmpPrice = tempCachemap.get(key);
            Price finalPrice = PriceCache.getLastPrice(key);
            if (finalPrice == null || finalPrice.getAsOf().compareTo(tmpPrice.getAsOf()) > 0) {
                PriceCache.add(tmpPrice);
            }
        });
    }

    @Override
    public boolean submitChunk(Chunk chunk) throws UnsupportedOperationException {
        final String batchId = chunk.getBatchid();
        synchronized (batchId) {
            if (!TempCache.tmpCacheContains(batchId)) {
                throw new UnsupportedOperationException(String.format("Bath with batchId %s is not yet started", batchId));
            }
            // Add all prices to BlockingQueue
            FutureTask<String> futureTask = new FutureTask<>(chunk);
            try {
                futureTaskBlockingQueue.put(futureTask);
            } catch (InterruptedException e) {
                LOG.severe(" interrupted while putting in to the queue, will do nothing as interruption not handled");
                return false;
            }
            addFutureToBatchIdFutureMap(batchId, futureTask);
        }
        return true;
    }

    private void addFutureToBatchIdFutureMap(String batchId, FutureTask futureTask) {
        List<FutureTask<String>> lst = batchIdFutureMap.get(batchId);
        if (lst == null) {
            lst = new ArrayList<>();
            lst.add(futureTask);
            batchIdFutureMap.put(batchId, lst);
            return;
        }
        lst.add(futureTask);
    }
}