package xxx.joker.libs.repo.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.repo.JkRepo;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.lambda.JkStreams.map;
import static xxx.joker.libs.core.util.JkConsole.displayColl;

public class RepoHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RepoHelper.class);

    public static List<Path> retrieveFilesWithNoResourceLink(JkRepo repo) {
        List<Path> linkedPaths = map(repo.getResources(), RepoResource::getPath);
        List<Path> allPaths = JkFiles.findFiles(repo.getRepoCtx().getResourcesFolder(), true);
        allPaths.removeAll(linkedPaths);
        return allPaths;
    }


}
