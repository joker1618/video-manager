package xxx.joker.libs.repo.jpa.proxy;

import xxx.joker.libs.repo.design.RepoEntity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

public class ProxyList implements InvocationHandler {

    private final List<RepoEntity> sourceList;
    private ReadWriteLock lock;
    private Function<RepoEntity, Boolean> addFunction;

    protected ProxyList(ReadWriteLock lock, Collection<RepoEntity> sourceData, Function<RepoEntity, Boolean> addFunction) {
        this.sourceList = new ArrayList<>(sourceData);
        this.lock = lock;
        this.addFunction  = addFunction;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        Lock actualLock = ProxyFactory.WRITE_METHODS_COLLECTION.contains(methodName) ? lock.writeLock() : lock.readLock();

        try {
            actualLock.lock();

            if ("add".equals(methodName)) {
                if(args.length == 1) {
                    RepoEntity e = (RepoEntity) args[0];
                    addFunction.apply(e);
                    return sourceList.add(e);
                } else {
                    int pos = (int) args[0];
                    RepoEntity e = (RepoEntity) args[1];
                    addFunction.apply(e);
                    sourceList.add(pos, e);
                    return null;
                }
            }

            if ("addAll".equals(methodName)) {
                if(args.length == 1) {
                    Collection<RepoEntity> coll = (Collection<RepoEntity>) args[0];
                    coll.forEach(addFunction::apply);
                    return sourceList.addAll(coll);
                } else {
                    int pos = (int) args[0];
                    Collection<RepoEntity> coll = (Collection<RepoEntity>) args[1];
                    coll.forEach(addFunction::apply);
                    return sourceList.addAll(pos, coll);
                }
            }

            if ("set".equals(methodName)) {
                int pos = (int) args[0];
                RepoEntity e = (RepoEntity) args[1];
                addFunction.apply(e);
                return sourceList.set(pos, e);
            }

            return method.invoke(sourceList, args);

        } finally {
            actualLock.unlock();
        }
    }

    public List<RepoEntity> getSourceList() {
        return sourceList;
    }
}