package xxx.joker.apps.video.manager.jfx.model;

import javafx.collections.ObservableList;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface FxModel {

    static FxModel getModel() {
        return FxModelImpl.getInstance();
    }

    ObservableList<Video> getVideos();
    Category getCategoryOrAdd(String catName);
    ObservableList<Category> getCategories();

    ObservableList<Video> getSelectedVideos();

    FxVideo toFxVideo(Video video);
    List<FxVideo> toFxVideos(Collection<Video> videos);
    FxVideo addVideoFile(Path videoPath, boolean skipIfPreviouslyAdded);

    String computeSafeTitle(String title);

    List<FxSnapshot> getSnapshots(Video video);
    List<FxSnapshot> getSnapshots(Video video, int numSnaps);
    void addSnapshot(Video video, JkDuration snapTime, Path snapPath);
    void removeSnapshot(Video video, JkDuration snapTime);
    void removeSnapshots(Video video);

    void exportVideos(Path outFolder, boolean insId, Collection<Video> videos);

    void persistData();

}
