package xxx.joker.libs.repo.jpa.indexes;

import xxx.joker.libs.repo.wrapper.RepoWField;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IndexManager {

    private ReentrantReadWriteLock lock;
    private boolean lockedSequence;
    private final AtomicLong mainSequence;
    // Data maintained to improve the performances in case of delete
    private Map<Long, Map<Long, Set<RepoWField>>> entitiesUsageInExternalCollections;

    public IndexManager(long mainSequence) {
        this.mainSequence = new AtomicLong(mainSequence);
        this.entitiesUsageInExternalCollections = new TreeMap<>();
        this.lock = new ReentrantReadWriteLock(true);
    }

    public synchronized void addUsage(Long usedBy, Long depId, RepoWField wf) {
        entitiesUsageInExternalCollections.putIfAbsent(usedBy, new TreeMap<>());
        entitiesUsageInExternalCollections.get(usedBy).putIfAbsent(depId, new HashSet<>());
        entitiesUsageInExternalCollections.get(usedBy).get(depId).add(wf);
    }

    public synchronized Map<Long, Set<RepoWField>> getEntityUsage(Long entityId) {
        return entitiesUsageInExternalCollections.getOrDefault(entityId, new TreeMap<>());
    }

    public synchronized void removeEntity(Long entityId) {
        entitiesUsageInExternalCollections.remove(entityId);
    }

    public synchronized void setSequenceValue(long value) {
        mainSequence.set(value);;
    }

    public synchronized long getSequenceValueAndLock() {
        lock.writeLock().lock();
        return getSequenceValue();
    }

    public synchronized void unlockSequence(boolean increment) {
        if(lock.isWriteLocked()) {
            if(increment)   mainSequence.getAndIncrement();
            lock.writeLock().unlock();
        }
    }

    public synchronized void clearUsages() {
        entitiesUsageInExternalCollections.clear();
    }

    public synchronized long getSequenceValue() {
        return mainSequence.get();
    }
}
