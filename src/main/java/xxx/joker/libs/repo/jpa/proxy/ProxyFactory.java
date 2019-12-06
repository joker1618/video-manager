package xxx.joker.libs.repo.jpa.proxy;

import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.wrapper.RepoWField;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

public class ProxyFactory {

    static final List<String> WRITE_METHODS_COLLECTION = Arrays.asList(
            "add", "addAll", "remove", "removeIf", "removeAll", "clear", "set", "forEach"
    );
    static final List<String> WRITE_METHODS_MAP = Arrays.asList(
            "put", "putAll", "putIfAbsent", "replace", "replaceAll", "compute", "computeIfPresent", "computeIfAbsent", "merge", "remove", "clear", "forEach"
    );

    private ReadWriteLock lock;
    private Function<RepoEntity, Boolean> addFunction;
    private Function<RepoEntity, Boolean> removeFunction;

    public ProxyFactory(ReadWriteLock lock, Function<RepoEntity, Boolean> addFunction, Function<RepoEntity, Boolean> removeFunction) {
        this.lock = lock;
        this.addFunction = addFunction;
        this.removeFunction = removeFunction;
    }

    public ProxyDataSet createProxyDataSet() {
        return createProxyDataSet(Collections.emptyList());
    }
    public ProxyDataSet createProxyDataSet(Collection<RepoEntity> data) {
        Collection<RepoEntity> finalData = data == null ? Collections.emptyList() : data;
        return new ProxyDataSet(lock, finalData, addFunction, removeFunction);
    }

    public Set<RepoEntity> createProxySet() {
        return createProxySet(Collections.emptyList());
    }
    public Set<RepoEntity> createProxySet(Collection<RepoEntity> data) {
        ClassLoader loader = TreeSet.class.getClassLoader();
        Class[] interfaces = {Set.class};
        Collection<RepoEntity> finalData = data == null ? Collections.emptyList() : data;
        ProxySet proxySet = new ProxySet(lock, finalData, addFunction);
        return (Set<RepoEntity>) Proxy.newProxyInstance(loader, interfaces, proxySet);
    }

    public List<RepoEntity> createProxyList() {
        return createProxyList(Collections.emptyList());
    }
    public List<RepoEntity> createProxyList(Collection<RepoEntity> data) {
        ClassLoader loader = ArrayList.class.getClassLoader();
        Class[] interfaces = {List.class};
        Collection<RepoEntity> finalData = data == null ? Collections.emptyList() : data;
        ProxyList proxyList = new ProxyList(lock, finalData, addFunction);
        return (List<RepoEntity>) Proxy.newProxyInstance(loader, interfaces, proxyList);
    }

    public Map<?, ?> createProxyMap(Map<?,?> map, RepoWField fw) {
        Class<?> mapClass = map instanceof TreeMap ? TreeMap.class : LinkedHashMap.class;
        ClassLoader loader = mapClass.getClassLoader();
        Class[] interfaces = {Map.class};
        Map<?,?> finalData = map == null ? new LinkedHashMap<>() : map;
        ProxyMap proxyMap = new ProxyMap(lock, finalData, addFunction, fw, this);
        return (Map<?,?>) Proxy.newProxyInstance(loader, interfaces, proxyMap);
    }
}
