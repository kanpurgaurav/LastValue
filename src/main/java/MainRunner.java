import data.Chunk;
import exception.UnsupportedOperationException;
import service.ProducerService;
import service.impl.ConsumerImpl;
import service.impl.ProducerImpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import static service.PriceProcessor.shutDown;
import static service.PriceProcessor.startProcessing;
import static util.ProducePriceList.produceListOfPrices;

public class MainRunner {

    private static final Logger LOG = Logger.getLogger(MainRunner.class.getName());

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<FutureTask<String>> blockingQueue = new ArrayBlockingQueue<>(1000);
        Map<String, List<FutureTask<String>>> cancelledFutureMap = new ConcurrentHashMap<>();

        // Producer producing
        ProducerService producer1 = new ProducerImpl(blockingQueue, cancelledFutureMap);
        ProducerService producer2 = new ProducerImpl(blockingQueue, cancelledFutureMap);
        ProducerService producer3 = new ProducerImpl(blockingQueue, cancelledFutureMap);
        String batchId = "Batch";
        LOG.info("Starting Batches");
        try {
            producer1.startProduction(batchId+ "-one");
            producer2.startProduction(batchId+ "-two");
            producer3.startProduction(batchId+ "-three");
        } catch (UnsupportedOperationException e) {
            LOG.info(e.getMessage());
        }

        //in memory Processing of produced results
        LOG.info("Starting Processor");
        startProcessing(blockingQueue);

        LOG.info("Submitting chunks in started Batches");
        for (int chnukCount = 0; chnukCount < 1000; chnukCount++) {
            try {
                producer1.submitChunk(new Chunk(batchId + "-one", produceListOfPrices(batchId+ "-one")));
                producer2.submitChunk(new Chunk(batchId + "-two", produceListOfPrices(batchId+ "-two")));
                producer3.submitChunk(new Chunk(batchId + "-three", produceListOfPrices(batchId+ "-three")));
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }

        // give 1000 ms for processing
        Thread.sleep(100);

        LOG.info("Marking completion of batches");
        //mark complete and cancel
        try {
            producer1.completeBatch(batchId + "-one");
            producer2.cancelBatch(batchId + "-two");
            producer3.completeBatch(batchId + "-three");
        } catch (UnsupportedOperationException e) {
            LOG.info(e.getMessage());
        }

        LOG.info("Consumer querying for prices");
        long time = System.currentTimeMillis();
        // consumer asking for various prices
        String id1 = String.format("id-%s-%s", batchId+"-one", "11");
        LOG.info(String.format("Price for PriceID %s is %s",id1, new ConsumerImpl().getPrice(id1)));

        String id2 = String.format("id-%s-%s", batchId+"-two", "21");
        LOG.info(String.format("Price for PriceID %s is %s",id2, new ConsumerImpl().getPrice(id2)));

        String id3 = String.format("id-%s-%s", batchId+"-three", "51");
        LOG.info(String.format("Price for PriceID %s is %s",id3, new ConsumerImpl().getPrice(id3)));

        LOG.info(String.format("Total time in querying is %s ms.",System.currentTimeMillis() - time));
        // Shut down processor
          shutDown();
          System.exit(0);
    }

}