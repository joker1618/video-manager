package xxx.joker.apps.video.manager.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.javalibs.exception.JkRuntimeException;
import xxx.joker.apps.video.manager.repository.entity.*;
import static xxx.joker.apps.video.manager.repository.JkPersistenceManager.EntityLines;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class JkDataModel {

    private static final Logger logger = LoggerFactory.getLogger(JkDataModel.class);

    private final JkPersistenceManager persistenceManager;
    private final JkEntityManager entityManager;
    private final Map<Class<?>, TreeSet<JkEntity>> dataMap;
    private final String pkgToScan;

    protected JkDataModel(Path dbFolder, String dbName, String pkgToScan) {
        logger.info("Initializing data model:  [dbName={}] [dbFolder={}] [pkgToScan={}]", dbName, dbFolder, pkgToScan);
        this.pkgToScan = pkgToScan;
        this.entityManager = new JkEntityManager(pkgToScan);
        this.persistenceManager = new JkPersistenceManager(dbFolder, dbName, entityManager.getEntityClasses());
        this.dataMap = readModelData();
    }

    private Map<Class<?>, TreeSet<JkEntity>> readModelData() {
        Map<Class<?>, EntityLines> elinesMap = persistenceManager.readData();
        return entityManager.parseData(elinesMap);
    }

    protected void commit() {
        Map<Class<?>, EntityLines> map = entityManager.formatData(dataMap);
        persistenceManager.saveData(map);
        logger.info("Committed model data");
    }

    protected <T extends JkEntity> TreeSet<T> getData(Class<T> entityClazz) {
        TreeSet<JkEntity> data = dataMap.get(entityClazz);
        if(data == null) {
            throw new JkRuntimeException("Class {} does not belong to package {}", entityClazz.getName(), pkgToScan);
        }
        return (TreeSet<T>) data;
    }

    public void cascadeDependencies() {
        dataMap.keySet().forEach(this::cascadeDependencies);
    }

    public void cascadeDependencies(Class<?> clazz) {
        dataMap.get(clazz).forEach(this::cascadeDependencies);
    }

    public void cascadeDependencies(JkEntity entity) {
        Map<Class<?>, Set<JkEntity>> dependencies = entityManager.getDependencies(entity);
        int counter = dependencies.values().stream().mapToInt(Set::size).sum();
        dependencies.forEach((k,v) -> dataMap.get(k).addAll(v));
        dependencies.forEach((k,v) -> v.forEach(this::cascadeDependencies));
        logger.trace("Spread {} broken dependencies for entity {}", counter, entity.getPrimaryKey());
    }

}
