package service.impl;

import cache.PriceCache;
import data.Price;
import service.ConsumerService;

import java.util.logging.Logger;

public class ConsumerImpl implements ConsumerService {

    private static final Logger LOG = Logger.getLogger(ConsumerImpl.class.getName());

    public Price getPrice(String id){
        if(id == null){
            LOG.info("No last price found");
            return null;
        }
        return PriceCache.getLastPrice(id);
    }
}
