package xxx.joker.apps.video.manager.datalayer;

import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.entities.VideoTracingAdded;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.repo.JkRepoFile;
import xxx.joker.libs.repo.design.entities.RepoResource;
import xxx.joker.libs.repo.design.entities.RepoTags;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VideoRepoImpl extends JkRepoFile implements VideoRepo {

    private static final VideoRepo instance = new VideoRepoImpl();

    private VideoRepoImpl() {
        super(Config.REPO_FOLDER, Config.DB_NAME, "xxx.joker.apps.video.manager.datalayer.entities");
    }

    static VideoRepo getInstance() {
        return instance;
    }

    @Override
    public Set<Video> getVideos() {
        return getDataSet(Video.class);
    }

    @Override
    public Set<Category> getCategories() {
        return getDataSet(Category.class);
    }

    @Override
    public Set<VideoTracingAdded> getAddedVideoHistory() {
        return getDataSet(VideoTracingAdded.class);
    }

//    @Override
//    public RepoResource getVideoResource(Video video) {
//        return getResource(video.getMd5(), "videoz");
//    }
//

    @Deprecated
    public Map<Video, RepoResource> getVideoResources(Collection<Video> videos) {
        List<RepoResource> resList = findResources("videoz");
        List<RepoResource> resFilter = JkStreams.filter(resList, res -> JkStreams.count(videos, v -> v.getMd5().equals(res.getMd5())) == 1);
        return JkStreams.toMapSingle(resFilter, res -> JkStreams.findUnique(videos, v -> v.getMd5().equals(res.getMd5())));
    }

    @Override
    public RepoResource addVideoResource(Video video, Path filePath) {
        return addResource(filePath, video.getMd5(), RepoTags.of("videoz"));
    }

    @Override
    public RepoResource getSnapshotResource(Video video, JkDuration snapTime) {
        return getResource(String.valueOf(snapTime.toMillis()), "snapshot " + video.getMd5());
    }

    @Override
    public List<RepoResource> getSnapshotResources(Video video) {
        List<RepoResource> resList = findResources("snapshot ", video.getMd5());
        return JkStreams.sorted(resList);
    }

    @Override
    public RepoResource addSnapshotResource(Video video, JkDuration snapTime, Path snapPath) {
        return addResource(snapPath, String.valueOf(snapTime.toMillis()), RepoTags.of("snapshot", video.getMd5()));
    }
}
