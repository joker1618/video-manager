package xxx.joker.libs.repo.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.file.JkEncryption;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.repo.config.RepoCtx;
import xxx.joker.libs.repo.design.entities.RepoResource;
import xxx.joker.libs.repo.design.entities.RepoTags;
import xxx.joker.libs.repo.design.entities.ResourceType;
import xxx.joker.libs.repo.exceptions.ErrorType;
import xxx.joker.libs.repo.exceptions.RepoError;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static xxx.joker.libs.core.util.JkStrings.strf;
import static xxx.joker.libs.repo.exceptions.ErrorType.*;

class ResourceHandlerImpl implements ResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceHandlerImpl.class);

    private final RepoCtx ctx;
    private final Set<RepoResource> resources;

    public ResourceHandlerImpl(RepoCtx ctx, Set<RepoResource> resources) {
        this.ctx = ctx;
        this.resources = resources;
    }

    /**
     * @return the resources with tags that belong to group of 'repoTags'
     */
    @Override
    public List<RepoResource> findResources(RepoTags repoTags) {
        return JkStreams.filter(resources, rr -> rr.getTags().belongToGroup(repoTags));
    }

    /**
     * @return the resource with equals name and tags
     */
    @Override
    public RepoResource getResource(String resName, RepoTags tags) {
        return JkStreams.findUnique(resources, rr -> rr.match(resName, tags));
    }

    @Override
    public RepoResource getOrAddResource(Path sourcePath, String resName, RepoTags tags, AddType addType) {
        RepoResource foundRes = getResource(resName, tags);
        String sourceMd5 = JkEncryption.getMD5(sourcePath);
        if (foundRes != null) {
            if (!foundRes.getMd5().equals(sourceMd5)) {
                throw new RepoError(RUN_ADD_EXISTING_RESOURCE, "Another resource with name='{}' and tags='{}' already exists");
            }
            return foundRes;
        }

        ResourceType resType = ResourceType.fromExtension(sourcePath);
        Path outPath = ctx.getResourcePath(sourceMd5, JkFiles.getExtension(sourcePath), resType);
        if(addType == AddType.MOVE) {
            JkFiles.move(sourcePath, outPath);
        } else {
            JkFiles.copy(sourcePath, outPath);
        }
        long size = JkFiles.sizeOf(outPath);

        RepoResource repoRes = new RepoResource();
        repoRes.setPath(outPath);
        repoRes.setSize(size);
        repoRes.setName(resName);
        repoRes.setTags(tags);
        repoRes.setMd5(sourceMd5);
        repoRes.setType(resType);
        resources.add(repoRes);

        return repoRes;
    }

    @Override
    public void exportResources(Path outFolder, RepoTags tags) {
        List<RepoResource> resources = findResources(tags);
        for (RepoResource res : resources) {
            String outName = strf("{}/{}/{}{}", res.getType(), res.getTags().format(), res.getName(), JkFiles.getExtension(res.getPath(), true));
            JkFiles.copy(res.getPath(), outFolder.resolve(outName));
        }
    }

}

