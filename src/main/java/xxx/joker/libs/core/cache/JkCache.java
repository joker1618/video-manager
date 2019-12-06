package xxx.joker.libs.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class JkCache<K, V> {

    private Map<K, V> cacheMap;

    public JkCache() {
        cacheMap = new HashMap<>();
    }

    public synchronized V get(K key) {
        return get(key, null);
    }

    public synchronized V get(K key, Supplier<V> creator) {
        V value = cacheMap.get(key);
        if(value == null && creator != null) {
            value = creator.get();
            cacheMap.put(key, value);
        }
        return value;
    }

    public synchronized void put(K key, V value) {
        cacheMap.put(key, value);
    }

    public synchronized boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

}
