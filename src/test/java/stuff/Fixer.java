package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkConsole.displayColl;

public class Fixer {

//    @Test
//    public void deleteUnusedResources() {
//        VideoRepo repo = VideoRepo.getRepo();
//
//        List<RepoResource> usedVideos = JkStreams.map(repo.getVideos(), repo::getVideoResource);
//        List<Path> existingPaths = JkFiles.findFiles(repo.getRepoCtx().getResourcesFolder().resolve("video"), false);
//        existingPaths.removeIf(p -> JkStreams.count(usedVideos, r -> JkFiles.areEquals(r.getPath(), p)) > 0);
//        display("Start deleting {} videos", existingPaths.size());
//        displayColl(existingPaths);
//        existingPaths.forEach(JkFiles::delete);
//
//        List<RepoResource> usedSnaps = repo.findResources("snapshot");
//        existingPaths = JkFiles.findFiles(repo.getRepoCtx().getResourcesFolder().resolve("image"), false);
//        existingPaths.removeIf(p -> JkStreams.count(usedSnaps, r -> JkFiles.areEquals(r.getPath(), p)) > 0);
//        display("Start deleting {} snapshots", existingPaths.size());
//        displayColl(existingPaths);
//        existingPaths.forEach(JkFiles::delete);
//    }
    @Test
    public void deleteVideoWithoutFile() {
        VideoRepo repo = VideoRepo.getRepo();
        List<Video> toRemove = JkStreams.filter(repo.getVideos(), v -> repo.getVideoResource(v) == null);
        toRemove.forEach(repo::remove);
        repo.commit();
        display("END");
    }
}
