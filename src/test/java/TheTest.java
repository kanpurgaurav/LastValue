import cache.PriceCache;
import cache.TempCache;
import data.Chunk;
import data.PayloadImpl;
import data.Price;
import data.PriceImpl;
import exception.UnsupportedOperationException;
import service.ProducerService;
import service.impl.ConsumerImpl;
import service.impl.ProducerImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

import static service.PriceProcessor.startProcessing;
import static util.ProducePriceList.produceListOfPrices;
import static org.testng.Assert.assertTrue;

public class TheTest {

    BlockingQueue<FutureTask<String>> arrayBlockingQueue = new ArrayBlockingQueue(1000);
    Map<String, List<FutureTask<String>>> cancelledFutureMap = new ConcurrentHashMap();
    ProducerService producer = new ProducerImpl(arrayBlockingQueue, cancelledFutureMap);
    String batchId = "Batch";

    @BeforeMethod
    public void setup() {
        TempCache.invalidate();
        PriceCache.invalidate();
    }


    @Test
    public void testStartProduction() throws UnsupportedOperationException {
        assertTrue(producer.startProduction(batchId));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldThrowOnReStartProduction() throws UnsupportedOperationException, InterruptedException {
        producer.startProduction(batchId);
        producer.startProduction(batchId);
    }

    @Test
    public void testProduction() throws InterruptedException, UnsupportedOperationException {
        produceAndProcess();
        Assert.assertEquals(TempCache.get(batchId).size(), 1000);
    }

    @Test
    public void testCompletion() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        producer.completeBatch(batchId);
        Assert.assertEquals(TempCache.get(batchId), null);
        Assert.assertEquals(PriceCache.size(), 1000);
    }

    @Test
    public void testCancellation() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        producer.cancelBatch(batchId);
        Assert.assertEquals(TempCache.get(batchId), null);
        Assert.assertEquals(PriceCache.size(), 0);
    }

    @Test
    public void shouldReturnPriceFromCompletedBatch() throws UnsupportedOperationException, InterruptedException {
        producer.startProduction(batchId);
        Price price = new PriceImpl(batchId, new Date(0, 0, 0), new PayloadImpl(999));
        List<Price> lst = new ArrayList<>();
        lst.add(price);
        producer.submitChunk(new Chunk(batchId, lst));
        startProcessing(arrayBlockingQueue);
        Thread.sleep(100);
        producer.completeBatch(batchId);
        Thread.sleep(100);
        Price priceFromCache = new ConsumerImpl().getPrice(batchId);
        Assert.assertEquals(price.equals(priceFromCache), true);
    }

    @Test
    public void shouldReturnNullFromCancelledBatch() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        producer.cancelBatch(batchId);
        Price priceFromCache = new ConsumerImpl().getPrice(batchId);
        Assert.assertEquals(priceFromCache, null);
    }

    @Test
    public void shouldReturnNullFromInCompleteBatch() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        Price priceFromCache = new ConsumerImpl().getPrice(batchId);
        Assert.assertEquals(priceFromCache, null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldThrowOnCancellingCompletedBatch() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        producer.completeBatch(batchId);
        producer.cancelBatch(batchId);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldThrowOnCompletingCancelledBatch() throws UnsupportedOperationException, InterruptedException {
        produceAndProcess();
        producer.cancelBatch(batchId);
        producer.completeBatch(batchId);
    }

    private void produceAndProcess() throws UnsupportedOperationException, InterruptedException {
        producer.startProduction(batchId);
        producer.submitChunk(new Chunk(batchId, produceListOfPrices(batchId)));
        startProcessing(arrayBlockingQueue);
        Thread.sleep(100);
    }
}