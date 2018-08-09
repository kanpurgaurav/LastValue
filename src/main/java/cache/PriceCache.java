package cache;

import data.Price;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PriceCache {

    private static Map<String, Price> map = new ConcurrentHashMap<>();

    private PriceCache() {
    }

    public static Price add(Price price) {
        return map.put(price.getId(), price);
    }

    public void addAll(Map<String, Price> priceMap) {
        map.putAll(priceMap);
    }

    public static Price getLastPrice(String id){
        return map.get(id);
    }

    public static int size() {
        return map.size();
    }

    public static void invalidate(){
        map.clear();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("You can not clone as there cannot be more than one instances for a price cache");
    }

}