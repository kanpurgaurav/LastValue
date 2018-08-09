package cache;

import data.Price;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TempCache {

    private static Map<String, Map<String, Price>> map = new ConcurrentHashMap<>();

    private TempCache() {
    }

    public static Price add(Price price, String batchId) {
        Map<String, Price> cc = map.get(batchId);
        if (null == cc) {
            cc = new ConcurrentHashMap<>();
            map.put(batchId, cc);
        }
        return cc.put(price.getId(), price);
    }

    public static Map<String, Price> get(String batchId) {
        return map.get(batchId);
    }

    public static void remove(String batchId) {
        map.remove(batchId);
    }

    public static void startBatch(String batchId) {
        map.put(batchId,  new ConcurrentHashMap<>());
    }

    public static boolean tmpCacheContains(String batchId) {
        return map.containsKey(batchId);
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