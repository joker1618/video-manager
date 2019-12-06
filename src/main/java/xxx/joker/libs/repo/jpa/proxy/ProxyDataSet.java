package xxx.joker.libs.repo.jpa.proxy;

import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.repo.design.RepoEntity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProxyDataSet implements InvocationHandler {

    private ReadWriteLock lock;
    private Function<RepoEntity, Boolean> addFunction;
    private Function<RepoEntity, Boolean> removeFunction;
    private final TreeSet<RepoEntity> sourceSet;
    private final Set<RepoEntity> proxyDataSet;

    protected ProxyDataSet(ReadWriteLock lock, Collection<RepoEntity> sourceData, Function<RepoEntity, Boolean> addFunction, Function<RepoEntity, Boolean> removeFunction) {
        this.sourceSet = new TreeSet<>(sourceData);
        this.lock = lock;
        this.addFunction  = addFunction;
        this.removeFunction = removeFunction;
        this.proxyDataSet = createProxyDataSet();
    }

    private Set<RepoEntity> createProxyDataSet() {
        ClassLoader loader = TreeSet.class.getClassLoader();
        Class[] interfaces = {Set.class};
        return (Set<RepoEntity>) Proxy.newProxyInstance(loader, interfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Lock actualLock = ProxyFactory.WRITE_METHODS_COLLECTION.contains(methodName) ? lock.writeLock() : lock.readLock();

        try {
            actualLock.lock();

            if ("add".equals(methodName)) {
                RepoEntity e = (RepoEntity) args[0];
                return addFunction.apply(e);
            }

            if ("addAll".equals(methodName)) {
                Collection<RepoEntity> coll = (Collection<RepoEntity>) args[0];
                boolean res = false;
                for (RepoEntity e : coll) {
                    res |= addFunction.apply(e);
                }
                return res;
            }

            if ("remove".equals(methodName)) {
                RepoEntity e = (RepoEntity) args[0];
                return removeFunction.apply(e);
            }

            if ("removeIf".equals(methodName)) {
                Predicate<RepoEntity> pred = (Predicate<RepoEntity>) args[0];
                List<RepoEntity> toRemove = JkStreams.filter(sourceSet, pred);
                boolean res = false;
                for (RepoEntity todel : toRemove) {
                    res |= removeFunction.apply(todel);
                }
                return res;
            }

            if ("removeAll".equals(methodName)) {
                Collection<RepoEntity> coll = (Collection<RepoEntity>) args[0];
                List<RepoEntity> eList = JkConvert.toList(coll);
                boolean res = false;
                for (RepoEntity todel : eList) {
                    res |= removeFunction.apply(todel);
                }
                return res;
            }

            if ("clear".equals(methodName)) {
                JkConvert.toList(sourceSet).forEach(e -> removeFunction.apply(e));
                return null;
            }

            return method.invoke(sourceSet, args);

        } finally {
            actualLock.unlock();
        }
    }

    public Set<RepoEntity> getProxyDataSet() {
        return proxyDataSet;
    }

    public <T extends RepoEntity> TreeSet<T> getEntities() {
        return (TreeSet<T>) sourceSet;
    }

}
