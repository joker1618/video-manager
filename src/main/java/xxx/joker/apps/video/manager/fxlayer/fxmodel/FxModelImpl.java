package xxx.joker.apps.video.manager.fxlayer.fxmodel;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FxModelImpl implements FxModel {

    private static final Logger LOG = LoggerFactory.getLogger(FxModelImpl.class);

    private static final FxModel instance = new FxModelImpl();

    private VideoRepo repo = VideoRepo.getRepo();

    private final ObservableList<Video> videos;
    private final ObservableList<Category> categories;
    private final ObservableList<Video> selectedVideos;

    private FxModelImpl() {
        videos = FXCollections.observableArrayList(repo.getVideos());
        categories = FXCollections.observableArrayList(repo.getCategories());
        selectedVideos = FXCollections.observableArrayList(new ArrayList<>());

        videos.addListener((ListChangeListener<? super Video>) lc -> {
            selectedVideos.removeIf(v -> !videos.contains(v));
            JkStreams.filter(videos, v -> !repo.getVideos().contains(v)).forEach(repo::add);
            JkStreams.filter(repo.getVideos(), v -> !videos.contains(v)).forEach(v -> {
                repo.removeResource(repo.getVideoResource(v));
                v.getSnapTimes().forEach(st -> repo.removeResource(repo.getSnapshotResource(v, st)));
                repo.remove(v);
            });
            persistData();
        });

        categories.addListener((ListChangeListener<? super Category>) lc -> {
            JkStreams.filter(categories, c -> !repo.getCategories().contains(c)).forEach(repo::add);
            JkStreams.filter(repo.getCategories(), c -> !categories.contains(c)).forEach(repo::remove);
            persistData();
        });
    }

    static FxModel getInstance() {
        return instance;
    }


    @Override
    public ObservableList<Video> getVideos() {
        return videos;
    }

    @Override
    public ObservableList<Category> getCategories() {
        return categories;
    }

    @Override
    public ObservableList<Video> getSelectedVideos() {
        return selectedVideos;
    }

    @Override
    public Path getVideoFile(Video video) {
        return repo.getVideoResource(video).getPath();
    }

    @Override
    public void addVideoFile(Video video, Path videoPath) {
        repo.addVideoResource(video, videoPath);
    }

    @Override
    public FxSnapshot getSnapshot(Video video, JkDuration snapTime) {
        RepoResource res = repo.getSnapshotResource(video, snapTime);
        FxSnapshot snap = new FxSnapshot();
        snap.setPath(res.getPath());
        snap.setImage(new Image(JkFiles.toURL(res.getPath())));
        snap.setTime(snapTime);
        return snap;
    }

    @Override
    public void addSnapshot(Video video, JkDuration snapTime, Path snapPath) {
        repo.addSnapshotResource(video, snapTime, snapPath);
        JkFiles.delete(snapPath);
    }

    @Override
    public void persistData() {
        repo.commit();
    }
}
