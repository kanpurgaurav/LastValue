package data;

import cache.TempCache;

import java.util.List;
import java.util.concurrent.Callable;

public class Chunk implements Callable {

    private final String batchid;

    private final List<Price> listOfPrices;
    public Chunk(String batchid, List<Price> listOfPrices) {
        this.batchid = batchid;
        this.listOfPrices = listOfPrices;
    }

    public String getBatchid() {
        return batchid;
    }

    @Override
    public Object call() throws Exception {
        listOfPrices.forEach(price -> TempCache.add(price, batchid));
        return "Success";
    }
}
