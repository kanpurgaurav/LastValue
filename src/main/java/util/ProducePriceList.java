package util;

import data.PayloadImpl;
import data.Price;
import data.PriceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProducePriceList {

    public static List<Price> produceListOfPrices(String batchId){
        // Producing price
            List<Price> list = new ArrayList<>(1000);
            int countInABatch =0;
            while(countInABatch++<1000){
                String id = String.format("id-%s-%s",batchId, String.valueOf(countInABatch));
                list.add(new PriceImpl(id, new Date(), new PayloadImpl(countInABatch)));
            }
        return list;
    }
}