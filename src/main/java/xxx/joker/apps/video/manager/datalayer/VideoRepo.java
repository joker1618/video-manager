package xxx.joker.apps.video.manager.datalayer;

import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.datalayer.JkRepo;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VideoRepo extends JkRepo {

    static VideoRepo getRepo() {
        return VideoRepoImpl.getInstance();
    }

    Set<Video> getVideos();
    Set<Category> getCategories();

    RepoResource getVideoResource(Video video);
    Map<Video, RepoResource> getVideoResources(Collection<Video> videos);
    RepoResource addVideoResource(Video video, Path filePath);

    RepoResource getSnapshotResource(Video video, JkDuration snapTime);
    List<RepoResource> getSnapshotResources(Video video);
    RepoResource addSnapshotResource(Video video, JkDuration snapTime, Path snapPath);
}
