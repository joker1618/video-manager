package xxx.joker.apps.video.manager.datalayer;

import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.HistoricalFileHash;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.repo.JkRepo;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface VideoRepo extends JkRepo {

    static VideoRepo getRepo() {
        return VideoRepoImpl.getInstance();
    }

    Set<Video> getVideos();
    Set<Category> getCategories();
    Set<HistoricalFileHash> getAddedVideoHistory();

    RepoResource addVideoResource(Video video, Path filePath);

    RepoResource getSnapshotResource(Video video, JkDuration snapTime);
    List<RepoResource> getSnapshotResources(Video video);
    RepoResource addSnapshotResource(Video video, JkDuration snapTime, Path snapPath);
}
