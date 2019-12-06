package xxx.joker.libs.core.util;

import xxx.joker.libs.core.lambda.JkStreams;

import java.util.*;
import java.util.function.Predicate;

public class JkStruct {

    public static <T> List<T> getDuplicates(Collection<T> source) {
        List<T> uniques = new ArrayList<>();
        List<T> dups = new ArrayList<>();
        for(T elem : source) {
            if(!uniques.contains(elem))     uniques.add(elem);
            else                            dups.add(elem);
        }
        return dups;
    }
    public static <T> List<T> getDuplicates(Collection<T> source, Comparator<T> comparator) {
        List<T> uniques = new ArrayList<>();
        List<T> dups = new ArrayList<>();
        for(T elem : source) {
            boolean found = !JkStreams.filter(uniques, u -> comparator.compare(u, elem) == 0).isEmpty();
            if(!found)	uniques.add(elem);
            else        dups.add(elem);
        }
        return dups;
    }

    @SafeVarargs
    public static <K,V> List<K> getMapKeys(Map<K,V> map, Predicate<V>... valueFilters) {
        List<K> keys = new ArrayList<>();
        map.entrySet().forEach(e -> {
            boolean res = true;
            for (Predicate<V> valueFilter : valueFilters) {
                res &= valueFilter.test(e.getValue());
            }
            if(res) {
                keys.add(e.getKey());
            }
        });
        return keys;
    }
    @SafeVarargs
    public static <K,V> List<V> getMapValues(Map<K,V> map, Predicate<K>... keyFilters) {
        List<V> values = new ArrayList<>();
        map.entrySet().forEach(e -> {
            boolean res = true;
            for (Predicate<K> keyFilter : keyFilters) {
                res &= keyFilter.test(e.getKey());
            }
            if(res) {
                values.add(e.getValue());
            }
        });
        return values;
    }

    public static <T> List<T> safeSublist(Collection<T> list, int from) {
        return list == null ? Collections.emptyList() : safeSublist(list, from, list.size());
    }
    public static <T> List<T> safeSublist(Collection<T> coll, int from, int to) {
        List<T> res = new ArrayList<>();
        if(coll != null) {
            List<T> source = new ArrayList<>(coll);
            if (from >= 0 && from < source.size()) {
                if (to > from) {
                    int end = Math.min(to, source.size());
                    res.addAll(source.subList(from, end));
                }
            }
        }
        return res;
    }

    public static <T> T getLastElem(Collection<T> coll) {
        List<T> list = JkConvert.toList(coll);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

}
