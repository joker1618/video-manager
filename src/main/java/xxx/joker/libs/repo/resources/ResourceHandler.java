package xxx.joker.libs.repo.resources;

import xxx.joker.libs.repo.config.RepoCtx;
import xxx.joker.libs.repo.design.entities.RepoResource;
import xxx.joker.libs.repo.design.entities.RepoTags;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface ResourceHandler {

    static ResourceHandler createHandler(RepoCtx ctx, Set<RepoResource> resources) {
        return new ResourceHandlerImpl(ctx, resources);
    }

    RepoResource getResource(String resName, RepoTags repoTags);
    List<RepoResource> findResources(RepoTags repoTags);

    RepoResource getOrAddResource(Path sourcePath, String resName, RepoTags repoTags, AddType addType);

    void exportResources(Path outFolder, RepoTags repoTags);

}