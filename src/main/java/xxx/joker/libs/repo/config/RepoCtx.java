package xxx.joker.libs.repo.config;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.design.entities.ResourceType;
import xxx.joker.libs.repo.wrapper.RepoWClazz;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static xxx.joker.libs.core.util.JkStrings.strf;
import static xxx.joker.libs.repo.config.RepoConfig.*;

public class RepoCtx {

    private Path repoFolder;
    private String dbName;
    private Map<Class<? extends RepoEntity>, RepoWClazz> wClazzMap;

    private ReadWriteLock repoLock;

    public RepoCtx(Path repoFolder, String dbName, Collection<RepoWClazz> wcList) {
        this.repoFolder = repoFolder;
        this.dbName = dbName;
        this.wClazzMap = JkStreams.toMapSingle(wcList, RepoWClazz::getEClazz);
        this.repoLock = new ReentrantReadWriteLock(true);
    }

    public Path getRepoFolder() {
        return repoFolder;
    }
    public Path getDbFolder() {
        return repoFolder.resolve(FOLDER_DB);
    }
    public Path getSourceJarsFolder() {
        return repoFolder.resolve(FOLDER_SOURCE_JARS);
    }
    public Path getResourcesFolder() {
        return repoFolder.resolve(FOLDER_RESOURCES);
    }
//    public Path getTempFolder() {
//        return repoFolder.resolve(FOLDER_TEMP);
//    }

    public String getDbName() {
        return dbName;
    }

    public Map<Class<? extends RepoEntity>, RepoWClazz> getWClazzMap() {
        return wClazzMap;
    }
    public RepoWClazz getWClazz(Class<?> clazz) {
        return wClazzMap.get(clazz);
    }

    public ReadWriteLock getLock() {
        return repoLock;
    }
    public Lock getReadLock() {
        return repoLock.readLock();
    }
    public Lock getWriteLock() {
        return repoLock.writeLock();
    }

    public Path getResourcePath(String md5, String extension, ResourceType resourceType) {
        String fname = md5;
        if(StringUtils.isNotBlank(extension)) fname += "." + extension.toLowerCase();
        return getResourcesFolder().resolve(resourceType.name().toLowerCase()).resolve(fname);
    }
    public Path getDbPath(RepoWClazz clazzWrap) {
        return getDbPath(clazzWrap.getEClazz().getSimpleName());
    }
    public Path getDbPath(String clazzSimpleName) {
        return getDbFolder().resolve(getDbEntityFileName(dbName, clazzSimpleName));
    }
    public Path getForeignKeysPath() {
        return getDbFolder().resolve(getDbFkeysFileName(dbName));
    }

}
