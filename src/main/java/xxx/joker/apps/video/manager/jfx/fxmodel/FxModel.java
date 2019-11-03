package xxx.joker.apps.video.manager.jfx.fxmodel;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;

public interface FxModel {

    static FxModel getModel() {
        return FxModelImpl.getInstance();
    }

    ObservableList<Video> getVideos();
    ObservableList<Category> getCategories();

    ObservableList<Video> getSelectedVideos();

    FxVideo toFxVideo(Video video);
    List<FxVideo> toFxVideos(Collection<Video> videos);
    FxVideo addVideoFile(Path videoPath, boolean skipIfPreviouslyAdded);

    List<FxSnapshot> getSnapshots(Video video);
    List<FxSnapshot> getSnapshots(Video video, int numSnaps);
    void addSnapshot(Video video, JkDuration snapTime, Path snapPath);
    void removeSnapshot(Video video, JkDuration snapTime);
    void removeSnapshots(Video video);

    void persistData();

}
