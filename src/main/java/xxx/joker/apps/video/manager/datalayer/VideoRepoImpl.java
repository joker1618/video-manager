package xxx.joker.apps.video.manager.datalayer;

import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.datalayer.JkRepoFile;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.nio.file.Path;
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
    public RepoResource getVideoResource(Video video) {
        return getResource(video.getMd5(), "videoz");
    }

    @Override
    public RepoResource addVideoResource(Video video, Path filePath) {
        return addResource(filePath, video.getMd5(), "videoz");
    }

    @Override
    public RepoResource getSnapshotResource(Video video, JkDuration snapTime) {
        return getResource(String.valueOf(snapTime.toMillis()), "snapshot " + video.getMd5());
    }

    @Override
    public RepoResource addSnapshotResource(Video video, JkDuration snapTime, Path snapPath) {
        return addResource(snapPath, String.valueOf(snapTime.toMillis()), "snapshot " + video.getMd5());
    }
}
